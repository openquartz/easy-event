package org.svnee.easyevent.transfer.kafka;

import static org.svnee.easyevent.common.utils.ParamUtils.checkNotNull;

import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.svnee.easyevent.common.concurrent.lock.LockBizType;
import org.svnee.easyevent.common.concurrent.lock.LockSupport;
import org.svnee.easyevent.common.model.Pair;
import org.svnee.easyevent.common.property.EasyEventProperties;
import org.svnee.easyevent.common.utils.JSONUtil;
import org.svnee.easyevent.transfer.api.EventTrigger;
import org.svnee.easyevent.transfer.api.limiting.EventTransferTriggerLimitingControl;
import org.svnee.easyevent.transfer.api.message.EventMessage;
import org.svnee.easyevent.transfer.kafka.common.KafkaTriggerProperty;
import org.svnee.easyevent.transfer.kafka.common.KafkaTriggerProperty.KafkaConsumerProperty;
import org.svnee.easyevent.transfer.kafka.property.KafkaCommonProperty;

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
    private final EasyEventProperties easyEventProperties;
    private final LockSupport lockSupport;
    private final EventTransferTriggerLimitingControl eventTransferTriggerLimitingControl;

    public KafkaEventTrigger(
        KafkaTriggerProperty kafkaTriggerProperty,
        KafkaCommonProperty kafkaCommonProperty,
        Consumer<EventMessage> eventHandler,
        EasyEventProperties easyEventProperties,
        LockSupport lockSupport,
        EventTransferTriggerLimitingControl eventTransferTriggerLimitingControl) {

        this.kafkaTriggerProperty = kafkaTriggerProperty;
        this.kafkaCommonProperty = kafkaCommonProperty;
        this.eventHandler = eventHandler;
        this.easyEventProperties = easyEventProperties;
        this.lockSupport = lockSupport;
        this.eventTransferTriggerLimitingControl = eventTransferTriggerLimitingControl;
    }

    @Override
    public void init() {
        synchronized (this) {
            kafkaTriggerProperty.getConsumerPropertyMap().forEach((k, v) -> {
                // TODO: 2023/2/24 to create consumer
            });
        }
    }

    @Override
    public void destroy() {
    }

    public void create(KafkaCommonProperty commonProperty,
        String identifyConsumer,
        List<KafkaConsumerProperty> consumerPropertyList) {
        KafkaConsumer<String, String> consumer;
        log.info("[RocketMqEventTrigger#create],properties:{},consumer-property:{}", commonProperty,
            consumerPropertyList);

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaCommonProperty.getHost());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerPropertyList.get(0).getConsumerGroup());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        consumer = new KafkaConsumer<>(props);
        List<TopicPartition> partitionList = consumerPropertyList.stream()
            .map(e -> new TopicPartition(e.getTopic(), Integer.parseInt(e.getPartition())))
            .collect(Collectors.toList());
        try {
            consumer.assign(partitionList);

            while (!Thread.currentThread().isInterrupted()) {
                ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
                for (ConsumerRecord<String, String> consumerRecord : records) {
                    EventMessage eventMessage = null;
                    try {
                        eventMessage = JSONUtil.parseObject(consumerRecord.value(), EventMessage.class);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                    checkNotNull(eventMessage);
                    // consume if lock
                    eventTransferTriggerLimitingControl.control(eventMessage,
                        msg -> lockSupport.consumeIfLock(
                            Pair.of(String.valueOf(msg.getEventId().getId()), LockBizType.EVENT_HANDLE),
                            () -> eventHandler.accept(msg)));
                }
                consumer.commitSync();
            }
        } catch (WakeupException e) {
            log.error(e.getMessage(), e);
        } finally {
            consumer.close();
        }
    }
}
