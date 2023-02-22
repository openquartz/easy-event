package org.svnee.easyevent.transfer.rocket;

import static org.svnee.easyevent.common.utils.ParamUtils.checkNotNull;

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
import org.svnee.easyevent.common.concurrent.lock.LockBizType;
import org.svnee.easyevent.common.concurrent.lock.LockSupport;
import org.svnee.easyevent.common.model.Pair;
import org.svnee.easyevent.common.property.EasyEventProperties;
import org.svnee.easyevent.common.utils.CollectionUtils;
import org.svnee.easyevent.common.utils.ExceptionUtils;
import org.svnee.easyevent.common.utils.IpUtil;
import org.svnee.easyevent.common.utils.JSONUtil;
import org.svnee.easyevent.transfer.api.EventTrigger;
import org.svnee.easyevent.transfer.api.limiting.EventTransferTriggerLimitingControl;
import org.svnee.easyevent.transfer.api.limiting.LimitingBlockedException;
import org.svnee.easyevent.transfer.api.message.EventMessage;
import org.svnee.easyevent.transfer.rocket.property.RocketMqCommonProperty;
import org.svnee.easyevent.transfer.rocket.property.RocketMqConsumerProperty;
import org.svnee.easyevent.transfer.rocket.property.RocketMqTriggerProperty;


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
                            message,
                            commonProperty, consumerProperty, ex);
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    } else {
                        log.error(
                            "[RocketMQTriggerConsumer#create]Consumption failure,max consume retry!message:{},properties:{},consumer-property:{}",
                            message,
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
                            message,
                            commonProperty, consumerProperty, ex);
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    } else {
                        log.error(
                            "[RocketMQTriggerConsumer#create]Consumption failure,max consume retry!message:{},properties:{},consumer-property:{}",
                            message,
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