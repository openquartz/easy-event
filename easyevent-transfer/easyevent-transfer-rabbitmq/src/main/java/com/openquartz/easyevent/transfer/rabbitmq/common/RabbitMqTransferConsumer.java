package com.openquartz.easyevent.transfer.rabbitmq.common;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP;
import com.openquartz.easyevent.transfer.api.message.EventMessage;
import com.openquartz.easyevent.common.utils.JSONUtil;
import com.openquartz.easyevent.common.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * RabbitMQ Transfer Consumer
 *
 * @author svnee
 */
@Slf4j
public class RabbitMqTransferConsumer extends DefaultConsumer {

    private final String queueName;
    private final String consumerGroup;
    private final Consumer<EventMessage> eventHandler;

    public RabbitMqTransferConsumer(Channel channel, 
                                  String queueName, 
                                  String consumerGroup,
                                  Consumer<EventMessage> eventHandler) {
        super(channel);
        this.queueName = queueName;
        this.consumerGroup = consumerGroup;
        this.eventHandler = eventHandler;
    }

    @Override
    public void handleDelivery(String consumerTag,
                               Envelope envelope,
                               AMQP.BasicProperties properties,
                               byte[] body) throws IOException {
        
        Channel channel = this.getChannel();
        long deliveryTag = envelope.getDeliveryTag();
        
        try {
            String messageBody = new String(body, "UTF-8");
            EventMessage eventMessage = JSONUtil.parseObject(messageBody, EventMessage.class);
            
            if (eventMessage != null && eventHandler != null) {
                eventHandler.accept(eventMessage);
            }
            
            // 手动确认消息
            channel.basicAck(deliveryTag, false);
            
            log.info("[RabbitMQ#handleDelivery] consume complete, queue:{}, consumerGroup:{}, deliveryTag:{}", 
                queueName, consumerGroup, deliveryTag);
        } catch (Exception ex) {
            log.error("[RabbitMQ#handleDelivery] consume error, queue:{}, consumerGroup:{}, deliveryTag:{}", 
                queueName, consumerGroup, deliveryTag, ex);
            
            try {
                // 拒绝消息并重新入队
                channel.basicNack(deliveryTag, false, true);
            } catch (Exception e) {
                log.error("[RabbitMQ#handleDelivery] nack error, queue:{}, consumerGroup:{}, deliveryTag:{}", 
                    queueName, consumerGroup, deliveryTag, e);
            }
            
            ExceptionUtils.rethrow(ex);
        }
    }
}