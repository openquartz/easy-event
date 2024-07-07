package com.openquartz.easyevent.starter.soa.rocketmq;

import com.openquartz.easyevent.common.property.EasyEventProperties;
import com.openquartz.easyevent.common.utils.CollectionUtils;
import com.openquartz.easyevent.common.utils.ExceptionUtils;
import com.openquartz.easyevent.common.utils.IpUtil;
import com.openquartz.easyevent.common.utils.JSONUtil;
import com.openquartz.easyevent.starter.soa.api.SoaEvent;
import com.openquartz.easyevent.storage.model.EventBody;
import com.openquartz.easyevent.transfer.api.EventTrigger;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotNull;

/**
 * rocketMQ event-trigger
 *
 * @author svnee
 **/
@Slf4j
public class SoaRocketMqEventTrigger implements EventTrigger {

    private final SoaEventRocketMqCommonProperty rocketMqCommonProperty;
    private final Consumer<SoaEvent> eventHandler;
    private final EasyEventProperties easyEventProperties;
    private final List<MQPushConsumer> consumerList = new ArrayList<>();

    public SoaRocketMqEventTrigger(
            SoaEventRocketMqCommonProperty rocketMqCommonProperty,
            Consumer<SoaEvent> eventHandler,
            EasyEventProperties easyEventProperties) {
        this.rocketMqCommonProperty = rocketMqCommonProperty;
        this.eventHandler = eventHandler;
        this.easyEventProperties = easyEventProperties;
    }

    @Override
    public void init() {
        synchronized (this) {
            MQPushConsumer consumer = create(rocketMqCommonProperty, "default");
            consumerList.add(consumer);
            try {
                consumer.start();
            } catch (MQClientException e) {
                log.error(
                        "[SoaRocketMqEventTrigger#init] init-consumer-error!common-property:{}",
                        rocketMqCommonProperty);
                ExceptionUtils.rethrow(e);
            }
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

    public MQPushConsumer create(SoaEventRocketMqCommonProperty commonProperty,
                                 String identifyConsumer) {
        DefaultMQPushConsumer consumer;
        log.info("[SoaRocketMqEventTrigger#create],properties:{}", commonProperty);
        try {
            consumer = new DefaultMQPushConsumer(commonProperty.getConsumeGroup());
            consumer.setMaxReconsumeTimes(1);
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            consumer.setConsumeThreadMin(commonProperty.getConsumeMinThreadNums());
            consumer.setConsumeThreadMax(commonProperty.getConsumeMaxThreadNums() + 1);
            consumer.setNamesrvAddr(commonProperty.getHost());
            consumer.subscribe(commonProperty.getTopic(), "*");

            String ipAddress = IpUtil.getIp();
            String[] split = ipAddress.split("\\.");

            consumer.setClientIP(ipAddress);
            consumer.setInstanceName(
                    easyEventProperties.getAppId() + "@" + UtilAll.getPid() + "@" + split[split.length - 1] + "@"
                            + identifyConsumer);
            //集群消费
            consumer.setMessageModel(MessageModel.CLUSTERING);
            consumer.setConsumeConcurrentlyMaxSpan(commonProperty.getConsumeConcurrentlyMaxSpan());
            // 开启内部类实现监听
            consumer.registerMessageListener((MessageListenerConcurrently) (messageExtList, context) -> {

                if (CollectionUtils.isEmpty(messageExtList)) {
                    log.warn("[SoaRocketMqEventTrigger#create]Message List is empty,properties:{},consumer-property:{}",
                            commonProperty, commonProperty);
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
                MessageExt message = messageExtList.get(0);
                try {
                    // json parse
                    @SuppressWarnings("unchecked")
                    EventBody<? extends SoaEvent> eventBody = JSONUtil.parseObject(message.getBody(), EventBody.class);
                    checkNotNull(eventBody);
                    assert eventBody != null;
                    eventHandler.accept(eventBody.getEvent());
                } catch (Throwable ex) {
                    if (message.getReconsumeTimes() < commonProperty.getConsumeMaxRetry()) {
                        // 消费时间间隔
                        context
                                .setDelayLevelWhenNextConsume(commonProperty.getConsumeRetryDelayTimeIntervalSeconds());
                        log.error(
                                "[SoaRocketMqEventTrigger#create]Consumption failure,message:{},properties:{},consumer-property:{}",
                                message.getMsgId(),
                                commonProperty, commonProperty, ex);
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    } else {
                        log.error(
                                "[SoaRocketMqEventTrigger#create]Consumption failure,max consume retry!message:{},properties:{},consumer-property:{}",
                                message.getMsgId(),
                                commonProperty, commonProperty, ex);
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }
                }
                log.info("[SoaRocketMqEventTrigger#create]Consumption Complete,rocketMsgId:{}", message.getMsgId());
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            });
        } catch (MQClientException ex) {
            log.error("[SoaRocketMqEventTrigger#create] create and start error,properties:{},consumer-property:{}",
                    commonProperty, commonProperty, ex);
            return ExceptionUtils.rethrow(ex);
        }
        log.info("[SoaRocketMqEventTrigger#create] create and start end,properties:{},consumer-property:{}",
                commonProperty, commonProperty);
        return consumer;
    }
}