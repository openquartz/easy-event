package com.openquartz.easyevent.starter.rabbitmq.spring.boot.autoconfig;

import com.openquartz.easyevent.common.concurrent.lock.LockSupport;
import com.openquartz.easyevent.common.serde.Serializer;
import com.openquartz.easyevent.common.transaction.TransactionSupport;
import com.openquartz.easyevent.core.trigger.AsyncEventHandler;
import com.openquartz.easyevent.starter.spring.boot.autoconfig.EasyEventAfterAutoConfiguration;
import com.openquartz.easyevent.starter.spring.boot.autoconfig.EasyEventTransferAutoConfiguration;
import com.openquartz.easyevent.starter.spring.boot.autoconfig.property.EasyEventCommonProperties;
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
import org.springframework.core.Ordered;

import java.util.concurrent.ExecutorService;

/**
 * RabbitMQ 自动配置
 *
 * @author svnee
 **/
@Slf4j
@EnableConfigurationProperties({RabbitMqCommonProperty.class, RabbitMqTriggerProperty.class})
@AutoConfigureBefore(EasyEventAfterAutoConfiguration.class)
@AutoConfigureAfter(EasyEventTransferAutoConfiguration.class)
@ConditionalOnClass(RabbitMqEventTrigger.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10050)
public class RabbitMqAutoConfiguration {

    public RabbitMqAutoConfiguration() {
        log.info("RabbitMqAutoConfiguration init >>>>>>>>>>>-------------");
    }

    @Bean
    @ConditionalOnMissingBean
    public RabbitMqTransferProducer rabbitMqTransferProducer(Serializer serializer,
                                                             EventRouter eventRouter,
                                                             RabbitMqCommonProperty rabbitMqCommonProperty) {
        return new RabbitMqTransferProducer(serializer, eventRouter, rabbitMqCommonProperty);
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    @ConditionalOnMissingBean
    public EventSender rabbitMqEventSender(RabbitMqCommonProperty rabbitMqCommonProperty,
                                           EventStorageService eventStorageService,
                                           @Autowired @Qualifier("asyncSendExecutor") ExecutorService asyncSendExecutor,
                                           TransactionSupport transactionSupport,
                                           RabbitMqTransferProducer rabbitMqTransferProducer,
                                           EventTransferSenderLimitingControl eventTransferSenderLimitingControl) {
        return new RabbitMqEventSender(
                rabbitMqCommonProperty,
                eventStorageService,
                asyncSendExecutor,
                transactionSupport,
                rabbitMqTransferProducer,
                eventTransferSenderLimitingControl
        );
    }

    @Bean(destroyMethod = "destroy", initMethod = "init")
    @ConditionalOnMissingBean
    public EventTrigger rabbitMqEventTrigger(RabbitMqTriggerProperty rabbitMqTriggerProperty,
                                             RabbitMqCommonProperty rabbitMqCommonProperty,
                                             AsyncEventHandler defaultAsyncEventHandler,
                                             EasyEventCommonProperties easyEventCommonProperties,
                                             LockSupport lockSupport,
                                             EventTransferTriggerLimitingControl eventTransferTriggerLimitingControl) {
        return new RabbitMqEventTrigger(
                rabbitMqTriggerProperty,
                rabbitMqCommonProperty,
                defaultAsyncEventHandler::handle,
                lockSupport,
                eventTransferTriggerLimitingControl
        );
    }
}