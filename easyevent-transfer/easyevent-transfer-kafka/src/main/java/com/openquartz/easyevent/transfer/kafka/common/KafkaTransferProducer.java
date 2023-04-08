package com.openquartz.easyevent.transfer.kafka.common;

import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotNull;

import com.openquartz.easyevent.transfer.kafka.exception.KafkaTransferErrorCode;
import com.openquartz.easyevent.transfer.kafka.property.KafkaCommonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import com.openquartz.easyevent.common.model.LifecycleBean;
import com.openquartz.easyevent.common.model.Pair;
import com.openquartz.easyevent.common.serde.Serializer;
import com.openquartz.easyevent.common.utils.Asserts;
import com.openquartz.easyevent.common.utils.CollectionUtils;
import com.openquartz.easyevent.common.utils.ExceptionUtils;
import com.openquartz.easyevent.common.utils.JSONUtil;
import com.openquartz.easyevent.common.utils.RandomUtils;
import com.openquartz.easyevent.storage.identify.EventId;
import com.openquartz.easyevent.transfer.api.adapter.TransferProducer;
import com.openquartz.easyevent.transfer.api.common.BatchSendResult;
import com.openquartz.easyevent.transfer.api.message.EventMessage;
import com.openquartz.easyevent.transfer.api.message.EventMessageBuilder;
import com.openquartz.easyevent.transfer.api.route.EventRouter;

/**
 * Kafka Transfer Producer
 *
 * @author svnee
 **/
@Slf4j
public class KafkaTransferProducer implements TransferProducer, LifecycleBean {

    private final org.apache.kafka.clients.producer.KafkaProducer<String, String> producer;
    private final Serializer serializer;
    private final EventRouter eventRouter;
    private final KafkaCommonProperty kafkaCommonProperty;

    public KafkaTransferProducer(Serializer serializer,
        EventRouter eventRouter,
        KafkaCommonProperty kafkaCommonProperty) {

        checkNotNull(serializer);
        checkNotNull(eventRouter);
        checkNotNull(kafkaCommonProperty);

        this.serializer = serializer;
        this.eventRouter = eventRouter;
        this.kafkaCommonProperty = kafkaCommonProperty;

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaCommonProperty.getHost());
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, kafkaCommonProperty.getProduceTimeout());
        props.put(ProducerConfig.RETRIES_CONFIG, kafkaCommonProperty.getProduceTryTimes());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            "org.apache.kafka.common.serialization.StringSerializer");
        this.producer = new org.apache.kafka.clients.producer.KafkaProducer<>(props);
    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {
        producer.close();
    }

    @Override
    public <T> void sendMessage(T event, EventId eventId) {

        EventMessage eventMessage = EventMessageBuilder.builder()
            .eventId(eventId)
            .event(event)
            .serializer(serializer)
            .build();
        Pair<String, String> routeTopic = eventRouter.route(event);

        int partition = parseRoutePartition(routeTopic);

        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(
            routeTopic.getKey(), partition, System.currentTimeMillis(), null, JSONUtil.toJson(eventMessage), null);
        try {
            Future<RecordMetadata> sendFuture = producer.send(producerRecord);
            sendFuture.get();
        } catch (InterruptedException ex) {
            log.error("[KafkaProducer#sendMessage] interrupted!event:{},eventId:{}", event, eventId, ex);
            Thread.currentThread().interrupt();
            ExceptionUtils.rethrow(ex);
        } catch (Exception ex) {
            log.error("[KafkaProducer#sendMessage] do-send-error!event:{},eventId:{}", event, eventId, ex);
            ExceptionUtils.rethrow(ex);
        }
    }

    private int parseRoutePartition(Pair<String, String> routeTopic) {
        int partition;
        if (Objects.isNull(routeTopic.getValue())) {
            partition = RandomUtils.nextInt(kafkaCommonProperty.getProduceTopicPartitions());
        } else {
            int currentPartition = Integer.parseInt(routeTopic.getValue());
            Asserts.isTrueIfLog(currentPartition < kafkaCommonProperty.getProduceTopicPartitions(),
                () -> log.error(
                    "[KafkaProducer#sendMessage] partition over outOfBounds! topic:{},partition:{},curPartition:{}",
                    routeTopic.getLeft(), kafkaCommonProperty.getProduceTopicPartitions(), currentPartition),
                KafkaTransferErrorCode.THE_SEND_PARTITION_OUT_OF_BOUNDS,
                routeTopic.getKey(), 0, kafkaCommonProperty.getProduceTopicPartitions(), currentPartition);
            partition = currentPartition;
        }
        return partition;
    }

    /**
     * 批量发送消息
     *
     * @param eventList eventList
     * @param eventIdList eventIdList
     * @param <T> T
     * @return result
     */
    @Override
    public <T> BatchSendResult sendMessageList(List<T> eventList, List<EventId> eventIdList) {
        if (CollectionUtils.isEmpty(eventList)) {
            return new BatchSendResult();
        }
        List<Pair<Integer, Object>> index2EventList = new ArrayList<>(eventList.size());
        for (int i = 0; i < eventList.size(); i++) {
            index2EventList.add(Pair.of(i, eventList.get(i)));
        }
        // mapping routeInfo 2 index
        Map<Pair<String, String>, List<Pair<Integer, Object>>> routeInfo2EventMap = index2EventList.stream()
            .map(e -> Pair.of(eventRouter.route(e.getValue()), e))
            .collect(Collectors.groupingBy(Pair::getKey, Collectors.mapping(Pair::getValue, Collectors.toList())));

        BatchSendResult batchSendResult = new BatchSendResult();

        for (Entry<Pair<String, String>, List<Pair<Integer, Object>>> routeInfo2Event : routeInfo2EventMap.entrySet()) {
            List<ProducerRecord<String, String>> messageList = routeInfo2Event.getValue()
                .stream()
                .map(e -> {
                    EventMessage eventMessage = EventMessageBuilder.builder()
                        .eventId(eventIdList.get(e.getKey()))
                        .event(e.getValue())
                        .serializer(serializer)
                        .build();
                    int partition = parseRoutePartition(routeInfo2Event.getKey());
                    return new ProducerRecord<String, String>(routeInfo2Event.getKey().getKey(),
                        partition, System.currentTimeMillis(), null, serializer.serialize(eventMessage), null);
                }).collect(Collectors.toList());

            List<Future<RecordMetadata>> futureList = new ArrayList<>();
            for (ProducerRecord<String, String> producerRecord : messageList) {
                futureList.add(producer.send(producerRecord));
            }

            for (int i = 0; i < futureList.size(); i++) {
                Pair<Integer, Object> eventIdPair = routeInfo2Event.getValue().get(i);
                try {
                    Future<RecordMetadata> future = futureList.get(i);
                    future.get();
                    batchSendResult.addCompletedIndex(eventIdPair.getKey());
                } catch (InterruptedException ex) {
                    log.error("[KafkaProducer#sendMessageList]interrupted!", ex);
                    Thread.currentThread().interrupt();
                } catch (Exception ex) {
                    log.error("[KafkaProducer#sendMessageList]send error!eventId:{},event:{}",
                        eventIdList.get(eventIdPair.getKey()), eventIdPair.getRight(), ex);
                    batchSendResult.addFailedIndex(eventIdPair.getKey());
                    batchSendResult.setFailedException(ex);
                }
            }
        }
        return batchSendResult;
    }

}
