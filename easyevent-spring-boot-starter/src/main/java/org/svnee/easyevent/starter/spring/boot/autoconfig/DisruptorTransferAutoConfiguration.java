package org.svnee.easyevent.starter.spring.boot.autoconfig;

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.svnee.easyevent.common.concurrent.lock.LockSupport;
import org.svnee.easyevent.common.serde.Serializer;
import org.svnee.easyevent.common.transaction.TransactionSupport;
import org.svnee.easyevent.core.trigger.AsyncEventHandler;
import org.svnee.easyevent.starter.spring.boot.autoconfig.property.DisruptorTransferProperties;
import org.svnee.easyevent.starter.spring.boot.autoconfig.property.DisruptorTransferProperties.DisruptorTransferConsumerProperty;
import org.svnee.easyevent.storage.api.EventStorageService;
import org.svnee.easyevent.transfer.api.EventSender;
import org.svnee.easyevent.transfer.disruptor.DisruptorTriggerEventSender;
import org.svnee.easyevent.transfer.disruptor.property.DisruptorConsumerProperty;
import org.svnee.easyevent.transfer.disruptor.property.DisruptorTriggerProperty;

/**
 * Disruptor Transfer AutoConfig
 *
 * @author svnee
 **/
@Slf4j
@EnableConfigurationProperties(DisruptorTransferProperties.class)
@AutoConfigureAfter(EasyEventTransferAutoConfiguration.class)
@ConditionalOnClass(DisruptorTriggerEventSender.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10020)
public class DisruptorTransferAutoConfiguration {

    @PostConstruct
    public void init() {
        log.info(
            "-----------------------------------------DisruptorTransferAutoConfiguration-------------------------------");
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    @ConditionalOnMissingBean
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
