package com.openquartz.easyevent.transfer.rabbitmq;

import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotNull;

import com.openquartz.easyevent.transfer.rabbitmq.property.RabbitMqCommonProperty;
import com.openquartz.easyevent.transfer.rabbitmq.property.RabbitMqTriggerProperty;
import com.openquartz.easyevent.transfer.rabbitmq.property.RabbitMqTriggerProperty.RabbitMqConsumerProperty;
import com.openquartz.easyevent.transfer.rabbitmq.common.RabbitMqTransferConsumer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Channel;
import com.openquartz.easyevent.common.concurrent.lock.LockBizType;
import com.openquartz.easyevent.common.concurrent.lock.LockSupport;
import com.openquartz.easyevent.common.model.Pair;
import com.openquartz.easyevent.common.utils.ExceptionUtils;
import com.openquartz.easyevent.transfer.api.EventTrigger;
import com.openquartz.easyevent.transfer.api.limiting.EventTransferTriggerLimitingControl;
import com.openquartz.easyevent.transfer.api.limiting.LimitingBlockedException;
import com.openquartz.easyevent.transfer.api.message.EventMessage;

/**
 * RabbitMQ Event Trigger
 *
 * @author svnee
 **/
@Slf4j
public class RabbitMqEventTrigger implements EventTrigger {

    private final RabbitMqTriggerProperty rabbitMqTriggerProperty;
    private final RabbitMqCommonProperty rabbitMqCommonProperty;
    private final Consumer<EventMessage> eventHandler;
    private final LockSupport lockSupport;
    private final EventTransferTriggerLimitingControl eventTransferTriggerLimitingControl;

    private final List<RabbitMqTransferConsumer> rabbitMqConsumerList = new ArrayList<>();
    private CountDownLatch countDownLatch;

    public RabbitMqEventTrigger(
        RabbitMqTriggerProperty rabbitMqTriggerProperty,
        RabbitMqCommonProperty rabbitMqCommonProperty,
        Consumer<EventMessage> eventHandler,
        LockSupport lockSupport,
        EventTransferTriggerLimitingControl eventTransferTriggerLimitingControl) {
        this.rabbitMqTriggerProperty = rabbitMqTriggerProperty;
        this.rabbitMqCommonProperty = rabbitMqCommonProperty;
        this.eventHandler = eventHandler;
        this.lockSupport = lockSupport;
        this.eventTransferTriggerLimitingControl = eventTransferTriggerLimitingControl;
    }

    @Override
    public void init() {
        synchronized (this) {
            // 计算总的消费者数量（考虑currency）
            int totalConsumerCount = rabbitMqTriggerProperty.getConsumerPropertyMap().values().stream()
                .mapToInt(RabbitMqConsumerProperty::getCurrency)
                .sum();
            this.countDownLatch = new CountDownLatch(totalConsumerCount);
            
            Map<String, List<RabbitMqConsumerProperty>> consumerGroup2RabbitMqConsumerMap = rabbitMqTriggerProperty
                .getConsumerPropertyMap()
                .values()
                .stream()
                .collect(Collectors.groupingBy(RabbitMqConsumerProperty::getConsumerGroup, Collectors.toList()));
            // create consume thread
            consumerGroup2RabbitMqConsumerMap.values().forEach(k -> {
                try {
                    create(rabbitMqCommonProperty, k, countDownLatch);
                } catch (Exception e) {
                    log.error("[RabbitMqEventTrigger#init] init-consumer-error!common-property:{},consumer-property-list:{}",
                        rabbitMqCommonProperty, k, e);
                    ExceptionUtils.rethrow(e);
                }
            });
            for (RabbitMqTransferConsumer consumer : rabbitMqConsumerList) {
                try {
                    consumer.start();
                } catch (Exception ex) {
                    log.error("[RabbitMqEventTrigger#init] consume-start!", ex);
                }
            }
        }
    }

    @Override
    public void destroy() {
        synchronized (this) {
            for (RabbitMqTransferConsumer consumer : rabbitMqConsumerList) {
                consumer.shutdown();
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException exception) {
                log.error("[RabbitMqEventTrigger#destroy] interrupted!", exception);
                Thread.currentThread().interrupt();
            }
        }
    }

    public void create(RabbitMqCommonProperty commonProperty,
        List<RabbitMqConsumerProperty> consumerPropertyList,
        CountDownLatch latch) throws Exception {

        // create consumer for each property
        for (RabbitMqConsumerProperty property : consumerPropertyList) {
            rabbitMqConsumerList.addAll(createConsumer(commonProperty, property, latch));
        }
    }

    private List<RabbitMqTransferConsumer> createConsumer(RabbitMqCommonProperty commonProperty,
        RabbitMqConsumerProperty rabbitMqConsumerProperty,
        CountDownLatch latch) throws Exception {

        List<RabbitMqTransferConsumer> consumerList = new ArrayList<>();
        
        // 根据并发数创建多个消费者
        for (int i = 0; i < rabbitMqConsumerProperty.getCurrency(); i++) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(commonProperty.getHost());
            factory.setPort(commonProperty.getPort());
            factory.setUsername(commonProperty.getUsername());
            factory.setPassword(commonProperty.getPassword());
            factory.setVirtualHost(commonProperty.getVirtualHost());
            
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            
            // 设置QoS
            channel.basicQos(rabbitMqConsumerProperty.getConsumeConcurrentlyMaxSpan());
            
            // 声明交换机
            if (rabbitMqConsumerProperty.getExchangeName() != null) {
                channel.exchangeDeclare(rabbitMqConsumerProperty.getExchangeName(), "topic", true);
            }
            
            // 声明队列
            channel.queueDeclare(rabbitMqConsumerProperty.getQueueName(), true, false, false, null);
            
            // 绑定队列到交换机
            if (rabbitMqConsumerProperty.getExchangeName() != null && rabbitMqConsumerProperty.getRoutingKey() != null) {
                channel.queueBind(rabbitMqConsumerProperty.getQueueName(), 
                                rabbitMqConsumerProperty.getExchangeName(), 
                                rabbitMqConsumerProperty.getRoutingKey());
            }
            
            RabbitMqTransferConsumer transferConsumer = new RabbitMqTransferConsumer(
                channel,
                connection,
                rabbitMqConsumerProperty.getQueueName(),
                rabbitMqConsumerProperty.getConsumerGroup(),
                latch,
                eventMessage -> {
                    checkNotNull(eventMessage);
                    
                    // consume if lock
                    eventTransferTriggerLimitingControl.control(eventMessage,
                        msg -> lockSupport.consumeIfLock(
                            Pair.of(String.valueOf(msg.getEventId().getId()), LockBizType.EVENT_HANDLE),
                            () -> eventHandler.accept(msg)));
                });
            consumerList.add(transferConsumer);
        }
        return consumerList;
    }
}