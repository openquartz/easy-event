package com.openquartz.easyevent.transfer.kafka;

import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import com.openquartz.easyevent.common.concurrent.lock.LockBizType;
import com.openquartz.easyevent.common.concurrent.lock.LockSupport;
import com.openquartz.easyevent.common.constant.CommonConstants;
import com.openquartz.easyevent.common.model.Pair;
import com.openquartz.easyevent.common.utils.Asserts;
import com.openquartz.easyevent.common.utils.CollectionUtils;
import com.openquartz.easyevent.common.utils.JSONUtil;
import com.openquartz.easyevent.transfer.api.EventTrigger;
import com.openquartz.easyevent.transfer.api.exception.TransferErrorCode;
import com.openquartz.easyevent.transfer.api.limiting.EventTransferTriggerLimitingControl;
import com.openquartz.easyevent.transfer.api.message.EventMessage;
import com.openquartz.easyevent.transfer.kafka.common.KafkaTransferConsumer;
import com.openquartz.easyevent.transfer.kafka.common.KafkaTriggerProperty;
import com.openquartz.easyevent.transfer.kafka.common.KafkaTriggerProperty.KafkaConsumerProperty;
import com.openquartz.easyevent.transfer.kafka.property.KafkaCommonProperty;

/**
 * KafkaEventTrigger
 *
 * @author svnee
 **/
@Slf4j
public class KafkaEventTrigger implements EventTrigger {

    private final KafkaTriggerProperty kafkaTriggerProperty;
    private final KafkaCommonProperty kafkaCommonProperty;
    private final Consumer<EventMessage> eventHandler;
    private final LockSupport lockSupport;
    private final EventTransferTriggerLimitingControl eventTransferTriggerLimitingControl;

    private final List<KafkaTransferConsumer> kafkaConsumerList = new ArrayList<>();
    private final CountDownLatch countDownLatch;

    public KafkaEventTrigger(
        KafkaTriggerProperty kafkaTriggerProperty,
        KafkaCommonProperty kafkaCommonProperty,
        Consumer<EventMessage> eventHandler,
        LockSupport lockSupport,
        EventTransferTriggerLimitingControl eventTransferTriggerLimitingControl) {

        this.kafkaTriggerProperty = kafkaTriggerProperty;
        this.countDownLatch = new CountDownLatch(kafkaTriggerProperty.getConsumerPropertyMap().size());
        this.kafkaCommonProperty = kafkaCommonProperty;
        this.eventHandler = eventHandler;
        this.lockSupport = lockSupport;
        this.eventTransferTriggerLimitingControl = eventTransferTriggerLimitingControl;
    }

    @Override
    public void init() {
        synchronized (this) {
            Map<String, List<KafkaConsumerProperty>> consumerGroup2KafkaConsumerMap = kafkaTriggerProperty
                .getConsumerPropertyMap()
                .values()
                .stream()
                .collect(Collectors.groupingBy(KafkaConsumerProperty::getConsumerGroup, Collectors.toList()));
            // create consume thread
            consumerGroup2KafkaConsumerMap.values().forEach(k -> create(kafkaCommonProperty, k, countDownLatch));
            for (KafkaTransferConsumer consumer : kafkaConsumerList) {
                try {
                    consumer.start();
                } catch (Exception ex) {
                    log.error("[KafkaEventTrigger#init]consume-start!", ex);
                }
            }
        }
    }

    @Override
    public void destroy() {
        synchronized (this) {
            for (KafkaTransferConsumer consumer : kafkaConsumerList) {
                consumer.shutdown();
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException exception) {
                log.error("[KafkaEventTrigger#destroy] interrupted!", exception);
                Thread.currentThread().interrupt();
            }
        }
    }

    public void create(KafkaCommonProperty commonProperty,
        List<KafkaConsumerProperty> consumerPropertyList,
        CountDownLatch latch) {

        // topic 2 partition
        Map<String, List<KafkaConsumerProperty>> map = consumerPropertyList
            .stream()
            .collect(Collectors.groupingBy(KafkaConsumerProperty::getTopic, Collectors.toList()));
        // topic
        for (Entry<String, List<KafkaConsumerProperty>> entry : map.entrySet()) {

            Map<String, List<KafkaConsumerProperty>> partition2KafkaConsumerPropertyMap = entry.getValue().stream()
                .collect(Collectors.groupingBy(e -> e.getPartition().trim(), Collectors.toList()));
            if (partition2KafkaConsumerPropertyMap.containsKey(CommonConstants.ALL_MATCH_EXPRESSION)) {
                Asserts.isTrueIfLog(partition2KafkaConsumerPropertyMap.keySet().size() == 1,
                    () -> log.error(
                        "[KafkaEventTrigger#create] same topic has all match expression,meantime has spec partition expression! partition:{}",
                        partition2KafkaConsumerPropertyMap.keySet()),
                    TransferErrorCode.CONSUMER_PARTITION_CONFIG_ILLEGAL);
            } else {
                partition2KafkaConsumerPropertyMap.values().forEach(k -> Asserts.isTrueIfLog(k.size() == 1,
                    () -> log.error(
                        "[KafkaEventTrigger#create] same topic has all match expression,meantime has spec partition expression! partition:{}",
                        k),
                    TransferErrorCode.CONSUMER_PARTITION_CONFIG_ILLEGAL));
            }
            // create consumer
            for (KafkaConsumerProperty property : entry.getValue()) {
                kafkaConsumerList.addAll(createConsumer(commonProperty.getHost(), property, latch));
            }
        }
    }

    private List<KafkaTransferConsumer> createConsumer(final String host,
        KafkaConsumerProperty kafkaConsumerProperty,
        CountDownLatch latch) {

        List<KafkaTransferConsumer> consumerList = new ArrayList<>();
        for (int i = 0; i < kafkaConsumerProperty.getCurrency(); i++) {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, host);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerProperty.getConsumerGroup());
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
            props.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG,
                kafkaConsumerProperty.getConsumeRetryDelayTimeIntervalSeconds() * 1000);

            if (kafkaConsumerProperty.getCurrency() == 1) {
                props.put(ConsumerConfig.CLIENT_ID_CONFIG, kafkaConsumerProperty.getClientId());
            } else {
                props.put(ConsumerConfig.CLIENT_ID_CONFIG, kafkaConsumerProperty.getClientId() + "-" + i);
            }
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

            // 通配
            if (Objects.equals(CommonConstants.ALL_MATCH_EXPRESSION, kafkaConsumerProperty.getPartition())) {
                consumer.subscribe(CollectionUtils.newArrayList(kafkaConsumerProperty.getTopic()));
            } else {
                consumer.assign(CollectionUtils.newArrayList(new TopicPartition(kafkaConsumerProperty.getTopic(),
                    Integer.parseInt(kafkaConsumerProperty.getPartition()))));
            }

            KafkaTransferConsumer transferConsumer = new KafkaTransferConsumer(kafkaConsumerProperty.getTopic(),
                kafkaConsumerProperty.getConsumerGroup(),
                consumer,
                latch,
                consumerRecord -> {
                    EventMessage eventMessage = null;
                    try {
                        eventMessage = JSONUtil.parseObject(consumerRecord.value(), EventMessage.class);
                    } catch (Exception e) {
                        log.error("[KafkaEventTrigger#consume] parse data error!data:{}", consumerRecord.value(), e);
                    }
                    checkNotNull(eventMessage);
                    // consume if lock
                    eventTransferTriggerLimitingControl.control(eventMessage,
                        msg -> lockSupport.consumeIfLock(
                            Pair.of(String.valueOf(msg.getEventId().getId()), LockBizType.EVENT_HANDLE),
                            () -> eventHandler.accept(msg)));
                });
            consumerList.add(transferConsumer);
        }
        return consumerList;
    }
}
