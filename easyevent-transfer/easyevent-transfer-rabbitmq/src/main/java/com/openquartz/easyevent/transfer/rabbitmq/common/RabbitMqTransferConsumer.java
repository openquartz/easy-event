package com.openquartz.easyevent.transfer.rabbitmq.common;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP;
import com.openquartz.easyevent.transfer.api.message.EventMessage;
import com.openquartz.easyevent.common.utils.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * RabbitMQ Transfer Consumer
 *
 * @author svnee
 */
@Slf4j
public class RabbitMqTransferConsumer extends Thread {

    private final Channel channel;
    private final Connection connection;
    private final String queueName;
    private final String consumerGroup;
    private final CountDownLatch latch;
    private final Consumer<EventMessage> eventHandler;
    private volatile boolean running = true;

    public RabbitMqTransferConsumer(Channel channel,
                                  Connection connection,
                                  String queueName,
                                  String consumerGroup,
                                  CountDownLatch latch,
                                  Consumer<EventMessage> eventHandler) {
        super("RabbitMqTransferConsumer-" + consumerGroup + "-" + queueName);
        this.channel = channel;
        this.connection = connection;
        this.queueName = queueName;
        this.consumerGroup = consumerGroup;
        this.latch = latch;
        this.eventHandler = eventHandler;
    }

    @Override
    public void run() {
        try {
            doWork();
        } catch (Exception e) {
            log.error("[RabbitMqTransferConsumer#run] Unexpected termination, exception thrown:", e);
        } finally {
            shutdown();
        }
    }

    private void doWork() {
        try {
            DefaultConsumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag,
                                         Envelope envelope,
                                         AMQP.BasicProperties properties,
                                         byte[] body) throws IOException {
                    long deliveryTag = envelope.getDeliveryTag();
                    
                    try {
                        String messageBody = new String(body, "UTF-8");
                        EventMessage eventMessage = JSONUtil.parseObject(messageBody, EventMessage.class);
                        
                        if (eventMessage != null && eventHandler != null) {
                            eventHandler.accept(eventMessage);
                        }
                        
                        // 手动确认消息
                        channel.basicAck(deliveryTag, false);
                        
                        log.debug("[RabbitMqTransferConsumer#handleDelivery] consume complete, queue:{}, consumerGroup:{}, deliveryTag:{}", 
                            queueName, consumerGroup, deliveryTag);
                    } catch (Exception ex) {
                        log.error("[RabbitMqTransferConsumer#handleDelivery] consume error, queue:{}, consumerGroup:{}, deliveryTag:{}", 
                            queueName, consumerGroup, deliveryTag, ex);
                        
                        try {
                            // 拒绝消息并重新入队
                            channel.basicNack(deliveryTag, false, true);
                        } catch (Exception e) {
                            log.error("[RabbitMqTransferConsumer#handleDelivery] nack error, queue:{}, consumerGroup:{}, deliveryTag:{}", 
                                queueName, consumerGroup, deliveryTag, e);
                        }
                    }
                }
            };
            
            // 开始消费
            channel.basicConsume(queueName, false, consumer);
            
            // 保持线程运行
            while (running && !Thread.currentThread().isInterrupted()) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            log.warn("[RabbitMqTransferConsumer#doWork] interrupted, queue:{}, group:{}", queueName, consumerGroup);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("[RabbitMqTransferConsumer#doWork] error, queue:{}, group:{}", queueName, consumerGroup, e);
        }
    }

    public void shutdown() {
        running = false;
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } catch (Exception ex) {
            log.error("[RabbitMqTransferConsumer#shutdown] close channel error!queue:{},group:{}", queueName, consumerGroup, ex);
        }
        try {
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        } catch (Exception ex) {
            log.error("[RabbitMqTransferConsumer#shutdown] close connection error!queue:{},group:{}", queueName, consumerGroup, ex);
        } finally {
            latch.countDown();
        }
    }
}