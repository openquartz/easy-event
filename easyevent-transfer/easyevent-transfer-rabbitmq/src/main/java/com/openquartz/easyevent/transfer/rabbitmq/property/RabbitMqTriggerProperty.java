package com.openquartz.easyevent.transfer.rabbitmq.property;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import com.openquartz.easyevent.common.utils.StringUtils;

/**
 * RabbitMQ Trigger Property
 *
 * @author svnee
 **/
@Data
public class RabbitMqTriggerProperty {

    /**
     * 消费者属性映射
     * key: 消费者标识
     * value: 消费者属性
     */
    private final Map<String, RabbitMqConsumerProperty> consumerPropertyMap = new ConcurrentHashMap<>();

    @Data
    public static class RabbitMqConsumerProperty {

        /**
         * 消费者组
         */
        private String consumerGroup;

        /**
         * 队列名称
         */
        private String queueName;

        /**
         * 交换机名称
         */
        private String exchangeName;

        /**
         * 路由键
         */
        private String routingKey;

        /**
         * 消费者并发数
         */
        private int currency = 1;

        /**
         * 消费者最小线程数
         */
        private int consumerMinThread = 1;

        /**
         * 消费者最大线程数
         */
        private int consumerMaxThread = 1;

        /**
         * 消费重试间隔时间(秒)
         */
        private int consumeRetryDelayTimeIntervalSeconds = 10;

        /**
         * 消费最大重试次数
         */
        private int consumeMaxRetry = 16;

        /**
         * 消费限流重试延迟时间基础秒数
         */
        private int consumeLimingRetryDelayTimeBaseSeconds = 2;

        /**
         * 消费并发最大跨度
         */
        private int consumeConcurrentlyMaxSpan = 2000;

        /**
         * 客户端ID
         */
        private String clientId = StringUtils.EMPTY;
    }
}