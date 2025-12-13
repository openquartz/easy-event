package com.openquartz.easyevent.starter.rabbitmq.spring.boot.autoconfig;

import java.util.Map;
import java.util.TreeMap;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RabbitMQ transfer 配置
 *
 * @author svnee
 **/
@Getter
@Slf4j
@ConfigurationProperties(prefix = RabbitMqTransferProperties.PREFIX)
public class RabbitMqTransferProperties {

    public static final String PREFIX = "easyevent.transfer.trigger.rabbitmq";

    /**
     * RabbitMQ host
     */
    @Setter
    private String host = "localhost";

    /**
     * RabbitMQ port
     */
    @Setter
    private int port = 5672;

    /**
     * RabbitMQ username
     */
    @Setter
    private String username = "guest";

    /**
     * RabbitMQ password
     */
    @Setter
    private String password = "guest";

    /**
     * RabbitMQ virtual host
     */
    @Setter
    private String virtualHost = "/";

    /**
     * 发送者组
     */
    @Setter
    public String produceGroup = "EasyEvent";

    /**
     * 使用mq 异步发送
     */
    @Setter
    public boolean produceAsync = false;

    /**
     * 发送超时
     * 单位：毫秒
     */
    @Setter
    private long produceTimeout = 3000L;

    /**
     * 重试次数
     */
    @Setter
    private int produceTryTimes = 3;

    /**
     * consumers
     */
    private final Map<String, RabbitMqTransferConsumerProperties> consumers = new TreeMap<>(
        String.CASE_INSENSITIVE_ORDER);

    public void setConsumers(Map<String, RabbitMqTransferConsumerProperties> consumers) {
        this.consumers.putAll(consumers);
    }

    /**
     * RabbitMQ Transfer ConsumerProperty
     *
     * @author svnee
     */
    @Setter
    @Getter
    public static class RabbitMqTransferConsumerProperties {

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
         * 并发数
         */
        private Integer currency = 1;

        /**
         * 消费消息最大重试次数
         */
        private Integer consumeMaxRetry = 5;

        /**
         * 消费重试最大时间间隔 单位：秒
         */
        private Integer consumeRetryDelayTimeIntervalSeconds = 10;

        /**
         * 消费限流重试延迟时间基础秒数
         */
        private Integer consumeLimingRetryDelayTimeBaseSeconds = 2;

        /**
         * 消费并发最大跨度
         */
        private Integer consumeConcurrentlyMaxSpan = 2000;

        /**
         * 客户端ID
         */
        private String clientId = "";

        @Override
        public String toString() {
            return "RabbitMqTransferConsumerProperties{" +
                "consumerGroup='" + consumerGroup + '\'' +
                ", queueName='" + queueName + '\'' +
                ", exchangeName='" + exchangeName + '\'' +
                ", routingKey='" + routingKey + '\'' +
                ", currency=" + currency +
                ", consumeMaxRetry=" + consumeMaxRetry +
                ", consumeRetryDelayTimeIntervalSeconds=" + consumeRetryDelayTimeIntervalSeconds +
                ", consumeConcurrentlyMaxSpan=" + consumeConcurrentlyMaxSpan +
                ", clientId='" + clientId + '\'' +
                '}';
        }
    }

    @Override
    public String toString() {
        return "RabbitMqTransferProperties{" +
            "host='" + host + '\'' +
            ", port=" + port +
            ", username='" + username + '\'' +
            ", virtualHost='" + virtualHost + '\'' +
            ", produceGroup='" + produceGroup + '\'' +
            ", produceAsync=" + produceAsync +
            ", produceTimeout=" + produceTimeout +
            ", produceTryTimes=" + produceTryTimes +
            ", consumers=" + consumers +
            '}';
    }
}

