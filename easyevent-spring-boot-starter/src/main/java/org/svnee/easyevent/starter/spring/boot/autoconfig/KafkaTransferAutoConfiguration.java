package org.svnee.easyevent.starter.spring.boot.autoconfig;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.svnee.easyevent.common.concurrent.lock.LockSupport;
import org.svnee.easyevent.common.serde.Serializer;
import org.svnee.easyevent.common.transaction.TransactionSupport;
import org.svnee.easyevent.core.trigger.AsyncEventHandler;
import org.svnee.easyevent.starter.spring.boot.autoconfig.property.KafkaTransferProperties;
import org.svnee.easyevent.storage.api.EventStorageService;
import org.svnee.easyevent.transfer.api.EventSender;
import org.svnee.easyevent.transfer.api.EventTrigger;
import org.svnee.easyevent.transfer.api.limiting.EventTransferSenderLimitingControl;
import org.svnee.easyevent.transfer.api.limiting.EventTransferTriggerLimitingControl;
import org.svnee.easyevent.transfer.api.route.EventRouteStrategy;
import org.svnee.easyevent.transfer.kafka.KafkaEventSender;
import org.svnee.easyevent.transfer.kafka.KafkaEventTrigger;
import org.svnee.easyevent.transfer.kafka.common.KafkaTransferProducer;
import org.svnee.easyevent.transfer.kafka.common.KafkaTriggerProperty;
import org.svnee.easyevent.transfer.kafka.common.KafkaTriggerProperty.KafkaConsumerProperty;
import org.svnee.easyevent.transfer.kafka.property.KafkaCommonProperty;

/**
 * Kafka Transfer AutoConfiguration
 *
 * @author svnee
 **/
@Slf4j
@EnableConfigurationProperties(KafkaTransferProperties.class)
@AutoConfigureAfter(EasyEventTransferAutoConfiguration.class)
@ConditionalOnClass(KafkaEventTrigger.class)
public class KafkaTransferAutoConfiguration {

    @Bean(initMethod = "init", destroyMethod = "destroy")
    @ConditionalOnMissingBean
    public EventSender eventSender(EventStorageService eventStorageService,
        ExecutorService asyncSendExecutor,
        TransactionSupport transactionSupport,
        KafkaTransferProducer kafkaTransferProducer,
        EventTransferSenderLimitingControl eventTransferSenderLimitingControl) {

        return new KafkaEventSender(kafkaTransferProducer, eventStorageService, asyncSendExecutor, transactionSupport,
            eventTransferSenderLimitingControl);
    }

    @Bean
    @ConditionalOnMissingBean
    public KafkaTransferProducer kafkaTransferProducer(
        Serializer serializer,
        EventRouteStrategy eventRouteStrategy,
        KafkaCommonProperty kafkaCommonProperty) {

        return new KafkaTransferProducer(serializer, eventRouteStrategy, kafkaCommonProperty);
    }

    @Bean
    public KafkaCommonProperty kafkaCommonProperty(KafkaTransferProperties kafkaTransferProperties) {

        KafkaCommonProperty kafkaCommonProperty = new KafkaCommonProperty();
        kafkaCommonProperty.setHost(kafkaTransferProperties.getHost());
        kafkaCommonProperty.setProduceTimeout(kafkaTransferProperties.getProduceTimeout());
        kafkaCommonProperty.setProduceTryTimes(kafkaTransferProperties.getProduceTryTimes());
        kafkaCommonProperty.setProduceTopicPartitions(kafkaTransferProperties.getProduceTopicPartitions());
        return kafkaCommonProperty;
    }

    @Bean
    public KafkaTriggerProperty kafkaTriggerProperty(
        KafkaTransferProperties kafkaTransferProperties) {

        Map<String, KafkaConsumerProperty> propertyMap = kafkaTransferProperties.getConsumers().entrySet()
            .stream()
            .collect(Collectors.toMap(Entry::getKey, e -> {
                KafkaConsumerProperty kafkaConsumerProperty = new KafkaConsumerProperty();
                kafkaConsumerProperty.setConsumerGroup(e.getValue().getConsumerGroup());
                kafkaConsumerProperty.setTopic(e.getValue().getTopic());
                kafkaConsumerProperty.setPartition(e.getValue().getPartition());
                kafkaConsumerProperty.setCurrency(e.getValue().getCurrency());
                kafkaConsumerProperty.setConsumeMaxRetry(e.getValue().getConsumeMaxRetry());
                kafkaConsumerProperty
                    .setConsumeRetryDelayTimeIntervalSeconds(e.getValue().getConsumeRetryDelayTimeIntervalSeconds());
                kafkaConsumerProperty.setClientId(e.getValue().getClientId());
                return kafkaConsumerProperty;
            }));
        KafkaTriggerProperty kafkaTriggerProperty = new KafkaTriggerProperty();
        kafkaTriggerProperty.setConsumerPropertyMap(propertyMap);
        return kafkaTriggerProperty;
    }

    @Bean(destroyMethod = "destroy", initMethod = "init")
    @ConditionalOnMissingBean
    public EventTrigger kafkaEventTrigger(KafkaCommonProperty kafkaCommonProperty,
        KafkaTriggerProperty kafkaTriggerProperty,
        AsyncEventHandler defaultAsyncEventHandler,
        LockSupport lockSupport,
        EventTransferTriggerLimitingControl eventTransferTriggerLimitingControl) {

        return new KafkaEventTrigger(kafkaTriggerProperty,
            kafkaCommonProperty,
            defaultAsyncEventHandler::handle,
            lockSupport,
            eventTransferTriggerLimitingControl);
    }


}
