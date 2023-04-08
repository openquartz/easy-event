package com.openquartz.easyevent.transfer.rocket;

import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotNull;

import com.openquartz.easyevent.transfer.rocket.property.RocketMqCommonProperty;
import com.openquartz.easyevent.transfer.rocket.property.RocketMqTriggerProperty;
import com.openquartz.easyevent.transfer.rocket.property.RocketMqTriggerProperty.RocketMqConsumerProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import com.openquartz.easyevent.common.concurrent.lock.LockBizType;
import com.openquartz.easyevent.common.concurrent.lock.LockSupport;
import com.openquartz.easyevent.common.model.Pair;
import com.openquartz.easyevent.common.property.EasyEventProperties;
import com.openquartz.easyevent.common.utils.CollectionUtils;
import com.openquartz.easyevent.common.utils.ExceptionUtils;
import com.openquartz.easyevent.common.utils.IpUtil;
import com.openquartz.easyevent.common.utils.JSONUtil;
import com.openquartz.easyevent.transfer.api.EventTrigger;
import com.openquartz.easyevent.transfer.api.limiting.EventTransferTriggerLimitingControl;
import com.openquartz.easyevent.transfer.api.limiting.LimitingBlockedException;
import com.openquartz.easyevent.transfer.api.message.EventMessage;

/**
 * rocketMQ event-trigger
 *
 * @author svnee
 **/
@Slf4j
public class RocketMqEventTrigger implements EventTrigger {

    private final RocketMqTriggerProperty rocketMqTriggerProperty;
    private final RocketMqCommonProperty rocketMqCommonProperty;
    private final Consumer<EventMessage> eventHandler;
    private final EasyEventProperties easyEventProperties;
    private final LockSupport lockSupport;
    private final EventTransferTriggerLimitingControl eventTransferTriggerLimitingControl;

    private final List<MQPushConsumer> consumerList = new ArrayList<>();

    public RocketMqEventTrigger(
        RocketMqTriggerProperty rocketMqTriggerProperty,
        RocketMqCommonProperty rocketMqCommonProperty,
        Consumer<EventMessage> eventHandler,
        EasyEventProperties easyEventProperties,
        LockSupport lockSupport,
        EventTransferTriggerLimitingControl eventTransferTriggerLimitingControl) {
        this.rocketMqTriggerProperty = rocketMqTriggerProperty;
        this.rocketMqCommonProperty = rocketMqCommonProperty;
        this.eventHandler = eventHandler;
        this.easyEventProperties = easyEventProperties;
        this.lockSupport = lockSupport;
        this.eventTransferTriggerLimitingControl = eventTransferTriggerLimitingControl;
    }

    @Override
    public void init() {
        synchronized (this) {
            rocketMqTriggerProperty.getConsumerPropertyMap().forEach((k, v) -> {
                MQPushConsumer consumer = create(rocketMqCommonProperty, k, v);
                consumerList.add(consumer);
                try {
                    consumer.start();
                } catch (MQClientException e) {
                    log.error(
                        "[RocketMqEventTrigger#init] init-consumer-error!common-property:{},identify-consumer:{},consumer-property:{}",
                        rocketMqCommonProperty, k, v);
                    ExceptionUtils.rethrow(e);
                }
            });
        }
    }

    @Override
    public void destroy() {
        synchronized (this) {
            for (MQPushConsumer consumer : consumerList) {
                consumer.shutdown();
            }
        }
    }

    public MQPushConsumer create(RocketMqCommonProperty commonProperty,
        String identifyConsumer,
        RocketMqConsumerProperty consumerProperty) {
        DefaultMQPushConsumer consumer = null;
        log.info("[RocketMqEventTrigger#create],properties:{},consumer-property:{}", commonProperty, consumerProperty);
        try {
            consumer = new DefaultMQPushConsumer(consumerProperty.getConsumerGroup());
            consumer.setMaxReconsumeTimes(1);
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            consumer.setConsumeThreadMin(consumerProperty.getConsumerMinThread());
            consumer.setConsumeThreadMax(consumerProperty.getConsumerMaxThread() + 1);
            consumer.setNamesrvAddr(commonProperty.getHost());
            consumer.subscribe(consumerProperty.getTopic(), consumerProperty.getConsumerTagSubExpression());

            String ipAddress = IpUtil.getIp();
            String[] split = ipAddress.split("\\.");

            consumer.setClientIP(ipAddress);
            consumer.setInstanceName(
                easyEventProperties.getAppId() + "@" + UtilAll.getPid() + "@" + split[split.length - 1] + "@"
                    + identifyConsumer);
            //集群消费
            consumer.setMessageModel(MessageModel.CLUSTERING);
            consumer.setConsumeConcurrentlyMaxSpan(consumerProperty.getConsumeConcurrentlyMaxSpan());
            // 开启内部类实现监听
            consumer.registerMessageListener((MessageListenerConcurrently) (messageExtList, context) -> {

                if (CollectionUtils.isEmpty(messageExtList)) {
                    log.warn("[RocketMQTriggerConsumer#create]Message List is empty,properties:{},consumer-property:{}",
                        commonProperty, consumerProperty);
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
                MessageExt message = messageExtList.get(0);
                try {
                    // json parse
                    EventMessage eventMessage = JSONUtil.parseObject(message.getBody(), EventMessage.class);
                    checkNotNull(eventMessage);
                    // consume if lock
                    eventTransferTriggerLimitingControl.control(eventMessage,
                        msg -> lockSupport.consumeIfLock(
                            Pair.of(String.valueOf(msg.getEventId().getId()), LockBizType.EVENT_HANDLE),
                            () -> eventHandler.accept(msg)));
                } catch (LimitingBlockedException ex) {
                    if (message.getReconsumeTimes() < consumerProperty.getConsumeMaxRetry()) {
                        // 消费时间间隔
                        context
                            .setDelayLevelWhenNextConsume(
                                consumerProperty.getConsumeLimingRetryDelayTimeBaseSeconds() * message
                                    .getReconsumeTimes());
                        log.error(
                            "[RocketMQTriggerConsumer#create]Consumption failure!message is blocked!,message:{},properties:{},consumer-property:{}",
                            message.getMsgId(),
                            commonProperty, consumerProperty, ex);
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    } else {
                        log.error(
                            "[RocketMQTriggerConsumer#create]Consumption failure,max consume retry!message:{},properties:{},consumer-property:{}",
                            message.getMsgId(),
                            commonProperty, consumerProperty, ex);
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }
                } catch (Throwable ex) {
                    if (message.getReconsumeTimes() < consumerProperty.getConsumeMaxRetry()) {
                        // 消费时间间隔
                        context
                            .setDelayLevelWhenNextConsume(consumerProperty.getConsumeRetryDelayTimeIntervalSeconds());
                        log.error(
                            "[RocketMQTriggerConsumer#create]Consumption failure,message:{},properties:{},consumer-property:{}",
                            message.getMsgId(),
                            commonProperty, consumerProperty, ex);
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    } else {
                        log.error(
                            "[RocketMQTriggerConsumer#create]Consumption failure,max consume retry!message:{},properties:{},consumer-property:{}",
                            message.getMsgId(),
                            commonProperty, consumerProperty, ex);
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }
                }
                log.info("[RocketMQTriggerConsumer#create]Consumption Complete,rocketMsgId:{}", message.getMsgId());
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            });
        } catch (MQClientException ex) {
            log.error("[RocketMQTriggerConsumer#create] create and start error,properties:{},consumer-property:{}",
                commonProperty, consumerProperty, ex);
            ExceptionUtils.rethrow(ex);
        }
        log.info("[RocketMQTriggerConsumer#create] create and start end,properties:{},consumer-property:{}",
            commonProperty, consumerProperty);
        return consumer;
    }
}