package com.openquartz.easyevent.starter.kafka.spring.boot.autoconfig;

import java.util.Map;
import java.util.TreeMap;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Kafka transfer 配置
 *
 * @author svnee
 **/
@Getter
@Slf4j
@ConfigurationProperties(prefix = KafkaTransferProperties.PREFIX)
public class KafkaTransferProperties {

    public static final String PREFIX = "easyevent.transfer.trigger.kafka";

    /**
     * host
     */
    @Setter
    private String host = "127.0.0.1:9092";

    /**
     * 发送者组
     */
    @Setter
    public String produceGroup = "EasyEvent";

    /**
     * 使用mq 异步发送
     */
    @Setter
    public boolean produceAsync = true;

    /**
     * 发送超时
     * 单位：秒
     */
    @Setter
    private Integer produceTimeout = 1000;

    /**
     * 重试次数
     */
    @Setter
    private int produceTryTimes = 5;

    /**
     * 生产者topic 的partition分区数
     */
    @Setter
    private int produceTopicPartitions = 2;

    /**
     * consumers
     */
    private final Map<String, KafkaTransferConsumerProperties> consumers = new TreeMap<>(
        String.CASE_INSENSITIVE_ORDER);

    public void setConsumers(Map<String, KafkaTransferConsumerProperties> consumers) {
        this.consumers.putAll(consumers);
    }

    /**
     * RocketMQ Transfer ConsumerProperty
     *
     * @author svnee
     */
    @Setter
    @Getter
    public static class KafkaTransferConsumerProperties {

        /**
         * 消费者组
         */
        private String consumerGroup;

        /**
         * clientId
         */
        private String clientId;

        /**
         * topic
         */
        private String topic = "";

        /**
         * partition
         */
        private String partition = "*";

        /**
         * 并发数
         */
        private Integer currency = 4;

        /**
         * 消费消息最大重试次数
         */
        private Integer consumeMaxRetry = 5;

        /**
         * 消费重试最大时间间隔 单位：秒
         */
        private Integer consumeRetryDelayTimeIntervalSeconds = 5;

        @Override
        public String toString() {
            return "KafkaTransferConsumerProperties{" +
                "consumerGroup='" + consumerGroup + '\'' +
                ", clientId='" + clientId + '\'' +
                ", topic='" + topic + '\'' +
                ", partition='" + partition + '\'' +
                ", currency=" + currency +
                ", consumeMaxRetry=" + consumeMaxRetry +
                ", consumeRetryDelayTimeIntervalSeconds=" + consumeRetryDelayTimeIntervalSeconds +
                '}';
        }
    }

    @Override
    public String toString() {
        return "KafkaTransferProperties{" +
            "host='" + host + '\'' +
            ", produceGroup='" + produceGroup + '\'' +
            ", produceTimeout=" + produceTimeout +
            ", produceTryTimes=" + produceTryTimes +
            ", produceTopicPartitions=" + produceTopicPartitions +
            ", consumers=" + consumers +
            '}';
    }
}
