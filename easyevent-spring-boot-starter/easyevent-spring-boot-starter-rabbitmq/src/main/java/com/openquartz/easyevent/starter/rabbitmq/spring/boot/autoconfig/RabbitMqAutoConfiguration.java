package com.openquartz.easyevent.starter.rabbitmq.spring.boot.autoconfig;

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
import com.openquartz.easyevent.transfer.rabbitmq.RabbitMqEventSender;
import com.openquartz.easyevent.transfer.rabbitmq.RabbitMqEventTrigger;
import com.openquartz.easyevent.transfer.rabbitmq.common.RabbitMqTransferProducer;
import com.openquartz.easyevent.transfer.rabbitmq.property.RabbitMqCommonProperty;
import com.openquartz.easyevent.transfer.rabbitmq.property.RabbitMqTriggerProperty;
import com.openquartz.easyevent.transfer.rabbitmq.property.RabbitMqTriggerProperty.RabbitMqConsumerProperty;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;
import org.springframework.core.Ordered;

import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

/**
 * RabbitMQ Transfer AutoConfiguration
 *
 * @author svnee
 **/
@Slf4j
@EnableConfigurationProperties(RabbitMqTransferProperties.class)
@AutoConfigureBefore(EasyEventAfterAutoConfiguration.class)
@AutoConfigureAfter(EasyEventTransferAutoConfiguration.class)
@ConditionalOnClass(RabbitMqEventTrigger.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10050)
public class RabbitMqAutoConfiguration {

    @Bean(initMethod = "init", destroyMethod = "destroy")
    @ConditionalOnMissingBean
    public EventSender eventSender(
            RabbitMqCommonProperty rabbitMqCommonProperty,
            EventStorageService eventStorageService,
            @Autowired @Qualifier("asyncSendExecutor") ExecutorService asyncSendExecutor,
            TransactionSupport transactionSupport,
            RabbitMqTransferProducer rabbitMqTransferProducer,
            EventTransferSenderLimitingControl eventTransferSenderLimitingControl) {

        return new RabbitMqEventSender(rabbitMqCommonProperty, eventStorageService, asyncSendExecutor, transactionSupport, rabbitMqTransferProducer, eventTransferSenderLimitingControl);
    }

    @Bean
    @ConditionalOnMissingBean
    public RabbitMqTransferProducer rabbitMqTransferProducer(
            Serializer serializer,
            EventRouter eventRouter,
            RabbitMqCommonProperty rabbitMqCommonProperty) {

        return new RabbitMqTransferProducer(serializer, eventRouter, rabbitMqCommonProperty);
    }

    @Bean
    @Role(ROLE_INFRASTRUCTURE)
    public RabbitMqCommonProperty rabbitMqCommonProperty(RabbitMqTransferProperties rabbitMqTransferProperties) {

        RabbitMqCommonProperty rabbitMqCommonProperty = new RabbitMqCommonProperty();
        rabbitMqCommonProperty.setHost(rabbitMqTransferProperties.getHost());
        rabbitMqCommonProperty.setPort(rabbitMqTransferProperties.getPort());
        rabbitMqCommonProperty.setUsername(rabbitMqTransferProperties.getUsername());
        rabbitMqCommonProperty.setPassword(rabbitMqTransferProperties.getPassword());
        rabbitMqCommonProperty.setVirtualHost(rabbitMqTransferProperties.getVirtualHost());
        rabbitMqCommonProperty.setProduceAsync(rabbitMqTransferProperties.isProduceAsync());
        rabbitMqCommonProperty.setProduceTimeout(rabbitMqTransferProperties.getProduceTimeout());
        rabbitMqCommonProperty.setProduceTryTimes(rabbitMqTransferProperties.getProduceTryTimes());
        return rabbitMqCommonProperty;
    }

    @Bean
    public RabbitMqTriggerProperty rabbitMqTriggerProperty(
            RabbitMqTransferProperties rabbitMqTransferProperties) {

        Map<String, RabbitMqConsumerProperty> propertyMap = rabbitMqTransferProperties.getConsumers().entrySet()
                .stream()
                .collect(Collectors.toMap(Entry::getKey, e -> {
                    RabbitMqConsumerProperty rabbitMqConsumerProperty = new RabbitMqConsumerProperty();
                    rabbitMqConsumerProperty.setConsumerGroup(e.getValue().getConsumerGroup());
                    rabbitMqConsumerProperty.setQueueName(e.getValue().getQueueName());
                    rabbitMqConsumerProperty.setExchangeName(e.getValue().getExchangeName());
                    rabbitMqConsumerProperty.setRoutingKey(e.getValue().getRoutingKey());
                    rabbitMqConsumerProperty.setCurrency(e.getValue().getCurrency());
                    rabbitMqConsumerProperty.setConsumeMaxRetry(e.getValue().getConsumeMaxRetry());
                    rabbitMqConsumerProperty
                            .setConsumeRetryDelayTimeIntervalSeconds(e.getValue().getConsumeRetryDelayTimeIntervalSeconds());
                    rabbitMqConsumerProperty.setConsumeLimingRetryDelayTimeBaseSeconds(e.getValue().getConsumeLimingRetryDelayTimeBaseSeconds());
                    rabbitMqConsumerProperty.setConsumeConcurrentlyMaxSpan(e.getValue().getConsumeConcurrentlyMaxSpan());
                    rabbitMqConsumerProperty.setClientId(e.getValue().getClientId());
                    return rabbitMqConsumerProperty;
                }));
        RabbitMqTriggerProperty rabbitMqTriggerProperty = new RabbitMqTriggerProperty();
        rabbitMqTriggerProperty.setConsumerPropertyMap(propertyMap);
        return rabbitMqTriggerProperty;
    }

    @Bean(destroyMethod = "destroy", initMethod = "init")
    @ConditionalOnMissingBean
    public EventTrigger rabbitMqEventTrigger(RabbitMqCommonProperty rabbitMqCommonProperty,
                                          RabbitMqTriggerProperty rabbitMqTriggerProperty,
                                          AsyncEventHandler defaultAsyncEventHandler,
                                          LockSupport lockSupport,
                                          EventTransferTriggerLimitingControl eventTransferTriggerLimitingControl) {

        return new RabbitMqEventTrigger(rabbitMqTriggerProperty,
                rabbitMqCommonProperty,
                defaultAsyncEventHandler::handle,
                lockSupport,
                eventTransferTriggerLimitingControl);
    }
}