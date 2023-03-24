package org.svnee.easyevent.starter.spring.boot.autoconfig.property;

import java.util.Map;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Kafka transfer 配置
 *
 * @author svnee
 **/
@Slf4j
@ConfigurationProperties(prefix = KafkaTransferProperties.PREFIX)
public class KafkaTransferProperties {

    public static final String PREFIX = "easyevent.transfer.trigger.kafka";

    /**
     * host
     */
    private String host = "127.0.0.1:9876";

    /**
     * 发送者组
     */
    public String produceGroup = "EasyEvent";

    /**
     * 发送超时
     * 单位：秒
     */
    private Integer produceTimeout = 1000;

    /**
     * 重试次数
     */
    private int produceTryTimes = 5;

    /**
     * 生产者topic 的partition分区数
     */
    private int produceTopicPartitions = 4;

    /**
     * consumers
     */
    private final Map<String, KafkaTransferConsumerProperties> consumers = new TreeMap<>(
        String.CASE_INSENSITIVE_ORDER);

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getProduceGroup() {
        return produceGroup;
    }

    public void setProduceGroup(String produceGroup) {
        this.produceGroup = produceGroup;
    }

    public Integer getProduceTimeout() {
        return produceTimeout;
    }

    public void setProduceTimeout(Integer produceTimeout) {
        this.produceTimeout = produceTimeout;
    }

    public int getProduceTryTimes() {
        return produceTryTimes;
    }

    public void setProduceTryTimes(int produceTryTimes) {
        this.produceTryTimes = produceTryTimes;
    }

    public int getProduceTopicPartitions() {
        return produceTopicPartitions;
    }

    public void setProduceTopicPartitions(int produceTopicPartitions) {
        this.produceTopicPartitions = produceTopicPartitions;
    }

    public Map<String, KafkaTransferConsumerProperties> getConsumers() {
        return consumers;
    }

    public void setConsumers(Map<String, KafkaTransferConsumerProperties> consumers) {
        this.consumers.putAll(consumers);
    }

    /**
     * RocketMQ Transfer ConsumerProperty
     *
     * @author svnee
     */
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

        public String getConsumerGroup() {
            return consumerGroup;
        }

        public void setConsumerGroup(String consumerGroup) {
            this.consumerGroup = consumerGroup;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getPartition() {
            return partition;
        }

        public void setPartition(String partition) {
            this.partition = partition;
        }

        public Integer getCurrency() {
            return currency;
        }

        public void setCurrency(Integer currency) {
            this.currency = currency;
        }

        public Integer getConsumeMaxRetry() {
            return consumeMaxRetry;
        }

        public void setConsumeMaxRetry(Integer consumeMaxRetry) {
            this.consumeMaxRetry = consumeMaxRetry;
        }

        public Integer getConsumeRetryDelayTimeIntervalSeconds() {
            return consumeRetryDelayTimeIntervalSeconds;
        }

        public void setConsumeRetryDelayTimeIntervalSeconds(Integer consumeRetryDelayTimeIntervalSeconds) {
            this.consumeRetryDelayTimeIntervalSeconds = consumeRetryDelayTimeIntervalSeconds;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

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
