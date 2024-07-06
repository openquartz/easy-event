package com.openquartz.easyevent.starter.rocketmq.spring.boot.autoconfig;

import com.openquartz.easyevent.common.concurrent.lock.LockSupport;
import com.openquartz.easyevent.common.serde.Serializer;
import com.openquartz.easyevent.common.transaction.TransactionSupport;
import com.openquartz.easyevent.common.utils.IpUtil;
import com.openquartz.easyevent.core.trigger.AsyncEventHandler;
import com.openquartz.easyevent.starter.spring.boot.autoconfig.EasyEventAfterAutoConfiguration;
import com.openquartz.easyevent.starter.spring.boot.autoconfig.EasyEventTransferAutoConfiguration;
import com.openquartz.easyevent.starter.spring.boot.autoconfig.property.EasyEventCommonProperties;
import com.openquartz.easyevent.storage.api.EventStorageService;
import com.openquartz.easyevent.transfer.api.EventSender;
import com.openquartz.easyevent.transfer.api.EventTrigger;
import com.openquartz.easyevent.transfer.api.constant.TransferConstants;
import com.openquartz.easyevent.transfer.api.limiting.EventTransferSenderLimitingControl;
import com.openquartz.easyevent.transfer.api.limiting.EventTransferTriggerLimitingControl;
import com.openquartz.easyevent.transfer.api.route.EventRouter;
import com.openquartz.easyevent.transfer.rocket.RocketMqEventSender;
import com.openquartz.easyevent.transfer.rocket.RocketMqEventTrigger;
import com.openquartz.easyevent.transfer.rocket.common.RocketMqProducer;
import com.openquartz.easyevent.transfer.rocket.property.RocketMqCommonProperty;
import com.openquartz.easyevent.transfer.rocket.property.RocketMqTriggerProperty;
import com.openquartz.easyevent.transfer.rocket.property.RocketMqTriggerProperty.RocketMqConsumerProperty;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.UtilAll;
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

/**
 * RocketMQ Transfer AutoConfiguration
 *
 * @author svnee
 **/
@Slf4j
@EnableConfigurationProperties(RocketTransferProperties.class)
@AutoConfigureBefore(EasyEventAfterAutoConfiguration.class)
@AutoConfigureAfter(EasyEventTransferAutoConfiguration.class)
@ConditionalOnClass(RocketMqEventTrigger.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10040)
public class RocketMqTransferAutoConfiguration {

    public RocketMqTransferAutoConfiguration() {
        log.info("RocketMqTransferAutoConfiguration init >>>>>>>>>>>-------------");
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    @ConditionalOnMissingBean
    public EventSender eventSender(EventStorageService eventStorageService,
                                   @Autowired @Qualifier("asyncSendExecutor") ExecutorService asyncSendExecutor,
                                   TransactionSupport transactionSupport,
                                   RocketMqProducer rocketMqProducer,
                                   EventTransferSenderLimitingControl eventTransferSenderLimitingControl) {

        return new RocketMqEventSender(eventStorageService, asyncSendExecutor, transactionSupport,
                rocketMqProducer, eventTransferSenderLimitingControl);
    }

    @Bean
    @ConditionalOnMissingBean
    public RocketMqProducer rocketMqProducer(MQProducer easyEventTriggerMqProducer,
                                             Serializer serializer,
                                             EventRouter eventRouter,
                                             RocketMqCommonProperty rocketMqCommonProperty) {

        return new RocketMqProducer(easyEventTriggerMqProducer, serializer, eventRouter, rocketMqCommonProperty);
    }

    @Bean
    @ConditionalOnMissingBean(value = MQProducer.class, name = "easyEventTriggerMqProducer")
    public MQProducer easyEventTriggerMqProducer(RocketTransferProperties transferProperties,
                                                 EasyEventCommonProperties easyEventCommonProperties) {

        DefaultMQProducer producer = new DefaultMQProducer(transferProperties.getProduceGroup());
        producer.setNamesrvAddr(transferProperties.getHost());
        producer.setVipChannelEnabled(false);
        producer.setRetryTimesWhenSendAsyncFailed(transferProperties.getProduceTryTimes());
        producer.setSendLatencyFaultEnable(transferProperties.isProduceLatencyFaultEnable());
        String ipAddress = IpUtil.getIp();
        String[] split = ipAddress.split("\\.");
        producer.setInstanceName(
                TransferConstants.SENDER_PREFIX + "@" + easyEventCommonProperties.getAppId() + "@" + split[split.length - 1]
                        + "@" + UtilAll.getPid());
        producer.setClientIP(ipAddress);
        return producer;
    }

    @Bean
    public RocketMqCommonProperty rocketMqCommonProperty(RocketTransferProperties rocketTransferProperties) {

        RocketMqCommonProperty rocketMqCommonProperty = new RocketMqCommonProperty();
        rocketMqCommonProperty.setHost(rocketTransferProperties.getHost());
        rocketMqCommonProperty.setProduceTimeout(rocketTransferProperties.getProduceTimeout());
        rocketMqCommonProperty.setProduceTryTimes(rocketTransferProperties.getProduceTryTimes());
        rocketMqCommonProperty.setProduceLatencyFaultEnable(rocketTransferProperties.isProduceLatencyFaultEnable());
        rocketMqCommonProperty.setProduceMessageSize(rocketTransferProperties.getProduceMessageSize());
        return rocketMqCommonProperty;
    }

    @Bean
    public RocketMqTriggerProperty rocketMqTriggerProperties(
            RocketTransferProperties rocketTransferProperties) {

        Map<String, RocketMqConsumerProperty> propertyMap = rocketTransferProperties.getConsumers().entrySet()
                .stream()
                .collect(Collectors.toMap(Entry::getKey, e -> {
                    RocketMqConsumerProperty rocketMqConsumerProperty = new RocketMqConsumerProperty();
                    rocketMqConsumerProperty.setConsumerGroup(e.getValue().getConsumerGroup());
                    rocketMqConsumerProperty.setConsumerMaxThread(e.getValue().getConsumerMaxThread());
                    rocketMqConsumerProperty.setConsumeConcurrentlyMaxSpan(e.getValue().getConsumeConcurrentlyMaxSpan());
                    rocketMqConsumerProperty.setConsumerMinThread(e.getValue().getConsumerMinThread());
                    rocketMqConsumerProperty.setTags(e.getValue().getTags());
                    rocketMqConsumerProperty.setTopic(e.getValue().getTopic());
                    rocketMqConsumerProperty.setConsumeMaxRetry(e.getValue().getConsumeMaxRetry());
                    rocketMqConsumerProperty.setConsumeLimingRetryDelayTimeBaseSeconds(e.getValue()
                            .getConsumeLimingRetryDelayTimeBaseSeconds());
                    rocketMqConsumerProperty
                            .setConsumeRetryDelayTimeIntervalSeconds(e.getValue().getConsumeRetryDelayTimeIntervalSeconds());
                    return rocketMqConsumerProperty;
                }));
        RocketMqTriggerProperty rocketMqTriggerProperty = new RocketMqTriggerProperty();
        rocketMqTriggerProperty.setConsumerPropertyMap(propertyMap);
        return rocketMqTriggerProperty;
    }

    @Bean(destroyMethod = "destroy", initMethod = "init")
    @ConditionalOnMissingBean
    public EventTrigger rocketMqEventTrigger(RocketMqCommonProperty rocketMqCommonProperty,
                                             RocketMqTriggerProperty rocketMqTriggerProperty,
                                             AsyncEventHandler defaultAsyncEventHandler,
                                             EasyEventCommonProperties easyEventCommonProperties,
                                             LockSupport lockSupport,
                                             EventTransferTriggerLimitingControl eventTransferTriggerLimitingControl) {

        return new RocketMqEventTrigger(rocketMqTriggerProperty,
                rocketMqCommonProperty,
                defaultAsyncEventHandler::handle,
                easyEventCommonProperties,
                lockSupport,
                eventTransferTriggerLimitingControl);
    }


}
