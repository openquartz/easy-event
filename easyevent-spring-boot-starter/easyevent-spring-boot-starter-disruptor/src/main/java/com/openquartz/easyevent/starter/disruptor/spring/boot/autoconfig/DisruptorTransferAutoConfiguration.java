package com.openquartz.easyevent.starter.disruptor.spring.boot.autoconfig;

import com.openquartz.easyevent.common.concurrent.lock.LockSupport;
import com.openquartz.easyevent.common.serde.Serializer;
import com.openquartz.easyevent.common.transaction.TransactionSupport;
import com.openquartz.easyevent.core.trigger.AsyncEventHandler;
import com.openquartz.easyevent.starter.disruptor.spring.boot.autoconfig.DisruptorTransferProperties.DisruptorTransferConsumerProperty;
import com.openquartz.easyevent.starter.spring.boot.autoconfig.EasyEventAfterAutoConfiguration;
import com.openquartz.easyevent.starter.spring.boot.autoconfig.EasyEventTransferAutoConfiguration;
import com.openquartz.easyevent.storage.api.EventStorageService;
import com.openquartz.easyevent.transfer.api.EventSender;
import com.openquartz.easyevent.transfer.disruptor.DisruptorTriggerEventSender;
import com.openquartz.easyevent.transfer.disruptor.property.DisruptorConsumerProperty;
import com.openquartz.easyevent.transfer.disruptor.property.DisruptorTriggerProperty;
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
 * Disruptor Transfer AutoConfig
 *
 * @author svnee
 **/
@Slf4j
@EnableConfigurationProperties(DisruptorTransferProperties.class)
@AutoConfigureBefore(EasyEventAfterAutoConfiguration.class)
@AutoConfigureAfter(EasyEventTransferAutoConfiguration.class)
@ConditionalOnClass(DisruptorTriggerEventSender.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10020)
public class DisruptorTransferAutoConfiguration {

    @Bean(initMethod = "init", destroyMethod = "destroy")
    @ConditionalOnMissingBean(EventSender.class)
    public EventSender eventSender(DisruptorTransferProperties properties,
        EventStorageService eventStorageService,
        TransactionSupport transactionSupport,
        Serializer serializer,
        AsyncEventHandler defaultAsyncEventHandler,
        LockSupport lockSupport) {

        return new DisruptorTriggerEventSender(convert(properties), defaultAsyncEventHandler::handle, serializer,
            transactionSupport, eventStorageService, lockSupport);
    }

    private DisruptorTriggerProperty convert(DisruptorTransferProperties properties) {
        DisruptorTriggerProperty disruptorTriggerProperty = new DisruptorTriggerProperty();
        disruptorTriggerProperty.setDisruptorThreadGroup(properties.getSender().getThreadGroup());
        disruptorTriggerProperty.setDisruptorThreadPrefix(properties.getSender().getThreadPrefix());
        disruptorTriggerProperty.setConsumerProperty(convertConsumerProperty(properties.getConsumer()));
        return disruptorTriggerProperty;
    }

    private DisruptorConsumerProperty convertConsumerProperty(DisruptorTransferConsumerProperty property) {
        DisruptorConsumerProperty disruptorConsumerProperty = new DisruptorConsumerProperty();
        disruptorConsumerProperty.setThreadPrefix(property.getThreadPrefix());
        disruptorConsumerProperty.setCorePoolSize(property.getCorePoolSize());
        disruptorConsumerProperty.setMaximumPoolSize(property.getMaximumPoolSize());
        disruptorConsumerProperty.setKeepAliveTime(property.getKeepAliveTime());
        disruptorConsumerProperty.setBufferSize(property.getBufferSize());
        return disruptorConsumerProperty;
    }
}
