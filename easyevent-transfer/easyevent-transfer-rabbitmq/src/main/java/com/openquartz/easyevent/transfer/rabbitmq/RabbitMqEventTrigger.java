package com.openquartz.easyevent.transfer.rabbitmq;

import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotNull;

import com.openquartz.easyevent.transfer.rabbitmq.property.RabbitMqCommonProperty;
import com.openquartz.easyevent.transfer.rabbitmq.property.RabbitMqTriggerProperty;
import com.openquartz.easyevent.transfer.rabbitmq.property.RabbitMqTriggerProperty.RabbitMqConsumerProperty;
import com.openquartz.easyevent.transfer.rabbitmq.common.RabbitMqTransferConsumer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Channel;
import com.openquartz.easyevent.common.concurrent.lock.LockBizType;
import com.openquartz.easyevent.common.concurrent.lock.LockSupport;
import com.openquartz.easyevent.common.model.Pair;
import com.openquartz.easyevent.common.utils.CollectionUtils;
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

    private final List<Connection> connectionList = new ArrayList<>();
    private final List<Channel> channelList = new ArrayList<>();

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
            rabbitMqTriggerProperty.getConsumerPropertyMap().forEach((k, v) -> {
                try {
                    create(rabbitMqCommonProperty, k, v);
                } catch (Exception e) {
                    log.error(
                        "[RabbitMqEventTrigger#init] init-consumer-error!common-property:{},identify-consumer:{},consumer-property:{}",
                        rabbitMqCommonProperty, k, v);
                    ExceptionUtils.rethrow(e);
                }
            });
        }
    }

    @Override
    public void destroy() {
        synchronized (this) {
            // 关闭所有channel
            for (Channel channel : channelList) {
                try {
                    if (channel.isOpen()) {
                        channel.close();
                    }
                } catch (Exception ex) {
                    log.error("[RabbitMqEventTrigger#destroy] channel close error", ex);
                }
            }
            
            // 关闭所有connection
            for (Connection connection : connectionList) {
                try {
                    if (connection.isOpen()) {
                        connection.close();
                    }
                } catch (Exception ex) {
                    log.error("[RabbitMqEventTrigger#destroy] connection close error", ex);
                }
            }
            
            channelList.clear();
            connectionList.clear();
        }
    }

    public void create(RabbitMqCommonProperty commonProperty,
        String identifyConsumer,
        RabbitMqConsumerProperty consumerProperty) throws Exception {
        
        log.info("[RabbitMqEventTrigger#create],properties:{},consumer-property:{}", commonProperty, consumerProperty);
        
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(commonProperty.getHost());
        factory.setPort(commonProperty.getPort());
        factory.setUsername(commonProperty.getUsername());
        factory.setPassword(commonProperty.getPassword());
        factory.setVirtualHost(commonProperty.getVirtualHost());
        
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        
        connectionList.add(connection);
        channelList.add(channel);
        
        // 设置QoS
        channel.basicQos(consumerProperty.getConsumeConcurrentlyMaxSpan());
        
        // 声明交换机
        if (consumerProperty.getExchangeName() != null) {
            channel.exchangeDeclare(consumerProperty.getExchangeName(), "topic", true);
        }
        
        // 声明队列
        channel.queueDeclare(consumerProperty.getQueueName(), true, false, false, null);
        
        // 绑定队列到交换机
        if (consumerProperty.getExchangeName() != null && consumerProperty.getRoutingKey() != null) {
            channel.queueBind(consumerProperty.getQueueName(), 
                            consumerProperty.getExchangeName(), 
                            consumerProperty.getRoutingKey());
        }
        
        // 创建消费者
        RabbitMqTransferConsumer consumer = new RabbitMqTransferConsumer(channel,
            consumerProperty.getQueueName(),
            consumerProperty.getConsumerGroup(),
            eventMessage -> {
                try {
                    // 消费限流控制
                    eventTransferTriggerLimitingControl.control(eventMessage,
                        msg -> lockSupport.consumeIfLock(
                            Pair.of(String.valueOf(msg.getEventId().getId()), LockBizType.EVENT_HANDLE),
                            () -> eventHandler.accept(msg)));
                } catch (LimitingBlockedException ex) {
                    log.error("[RabbitMqEventTrigger#consume] Consumption failure! message is blocked!,queue:{},consumerGroup:{}",
                        consumerProperty.getQueueName(), consumerProperty.getConsumerGroup(), ex);
                    throw ex;
                } catch (Throwable ex) {
                    log.error("[RabbitMqEventTrigger#consume] Consumption failure,queue:{},consumerGroup:{}",
                        consumerProperty.getQueueName(), consumerProperty.getConsumerGroup(), ex);
                    throw ex;
                }
            });
        
        // 开启消费
        channel.basicConsume(consumerProperty.getQueueName(), false, consumer);
        
        log.info("[RabbitMqEventTrigger#create] create and start end,properties:{},consumer-property:{}",
            commonProperty, consumerProperty);
    }
}