package com.openquartz.easyevent.starter.kafka.spring.boot.autoconfig;

import com.openquartz.easyevent.common.concurrent.lock.LockSupport;
import com.openquartz.easyevent.common.serde.Serializer;
import com.openquartz.easyevent.common.transaction.TransactionSupport;
import com.openquartz.easyevent.core.trigger.AsyncEventHandler;
import com.openquartz.easyevent.starter.spring.boot.autoconfig.EasyEventAfterAutoConfiguration;
import com.openquartz.easyevent.starter.spring.boot.autoconfig.EasyEventTransferAutoConfiguration;
import com.openquartz.easyevent.storage.api.EventStorageService;
import com.openquartz.easyevent.transfer.api.EventSender;
import com.openquartz.easyevent.transfer.api.EventTrigger;
import com.openquartz.easyevent.transfer.api.limiting.EventTransferSenderLimitingControl;
import com.openquartz.easyevent.transfer.api.limiting.EventTransferTriggerLimitingControl;
import com.openquartz.easyevent.transfer.api.route.EventRouter;
import com.openquartz.easyevent.transfer.kafka.KafkaEventSender;
import com.openquartz.easyevent.transfer.kafka.KafkaEventTrigger;
import com.openquartz.easyevent.transfer.kafka.common.KafkaTransferProducer;
import com.openquartz.easyevent.transfer.kafka.common.KafkaTriggerProperty;
import com.openquartz.easyevent.transfer.kafka.common.KafkaTriggerProperty.KafkaConsumerProperty;
import com.openquartz.easyevent.transfer.kafka.property.KafkaCommonProperty;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * Kafka Transfer AutoConfiguration
 *
 * @author svnee
 **/
@Slf4j
@EnableConfigurationProperties(KafkaTransferProperties.class)
@AutoConfigureBefore(EasyEventAfterAutoConfiguration.class)
@AutoConfigureAfter(EasyEventTransferAutoConfiguration.class)
@ConditionalOnClass(KafkaEventTrigger.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10060)
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
        EventRouter eventRouter,
        KafkaCommonProperty kafkaCommonProperty) {

        return new KafkaTransferProducer(serializer, eventRouter, kafkaCommonProperty);
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
