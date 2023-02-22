package org.svnee.easyevent.starter.spring.boot.autoconfig;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.UtilAll;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.svnee.easyevent.common.concurrent.lock.LockSupport;
import org.svnee.easyevent.common.serde.Serializer;
import org.svnee.easyevent.common.transaction.TransactionSupport;
import org.svnee.easyevent.common.utils.IpUtil;
import org.svnee.easyevent.core.trigger.AsyncEventHandler;
import org.svnee.easyevent.starter.spring.boot.autoconfig.property.EasyEventCommonProperties;
import org.svnee.easyevent.starter.spring.boot.autoconfig.property.RocketTransferProperties;
import org.svnee.easyevent.storage.api.EventStorageService;
import org.svnee.easyevent.transfer.api.EventSender;
import org.svnee.easyevent.transfer.api.EventTrigger;
import org.svnee.easyevent.transfer.api.constant.TransferConstants;
import org.svnee.easyevent.transfer.api.limiting.EventTransferSenderLimitingControl;
import org.svnee.easyevent.transfer.api.limiting.EventTransferTriggerLimitingControl;
import org.svnee.easyevent.transfer.api.rocket.RocketMqEventSender;
import org.svnee.easyevent.transfer.api.rocket.RocketMqEventTrigger;
import org.svnee.easyevent.transfer.api.rocket.common.RocketMqProducer;
import org.svnee.easyevent.transfer.api.rocket.property.RocketMqCommonProperty;
import org.svnee.easyevent.transfer.api.rocket.property.RocketMqConsumerProperty;
import org.svnee.easyevent.transfer.api.rocket.property.RocketMqTriggerProperty;
import org.svnee.easyevent.transfer.api.route.EventRouteStrategy;

/**
 * RocketMQ Transfer AutoConfiguration
 *
 * @author svnee
 **/
@Slf4j
@EnableConfigurationProperties(RocketTransferProperties.class)
@AutoConfigureAfter(EasyEventTransferAutoConfiguration.class)
@ConditionalOnClass(RocketMqEventTrigger.class)
public class RocketMqTransferAutoConfiguration {

    @Bean(initMethod = "init", destroyMethod = "destroy")
    @ConditionalOnMissingBean
    public EventSender eventSender(EventStorageService eventStorageService,
        ExecutorService asyncSendExecutor,
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
        EventRouteStrategy eventRouteStrategy,
        RocketMqCommonProperty rocketMqCommonProperty) {

        return new RocketMqProducer(easyEventTriggerMqProducer, serializer, eventRouteStrategy, rocketMqCommonProperty);
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
            TransferConstants.SENDER_PREFIX + "@" + easyEventCommonProperties.getAppId() + "@" + split[split.length
                - 1]
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
