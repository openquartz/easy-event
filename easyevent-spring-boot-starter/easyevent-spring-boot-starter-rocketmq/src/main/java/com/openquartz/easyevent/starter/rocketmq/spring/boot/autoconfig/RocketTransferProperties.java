package com.openquartz.easyevent.starter.rocketmq.spring.boot.autoconfig;

import java.util.Map;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RocketMQ transfer 配置
 *
 * @author svnee
 **/
@Slf4j
@ConfigurationProperties(prefix = RocketTransferProperties.PREFIX)
public class RocketTransferProperties {

    public static final String PREFIX = "easyevent.transfer.trigger.rocketmq";

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
     * 故障轉移
     */
    private boolean produceLatencyFaultEnable = true;

    /**
     * produce message size
     */
    private int produceMessageSize = 1000 * 1000;

    /**
     * consumers
     */
    private final Map<String, RocketMqTransferConsumerProperties> consumers = new TreeMap<>(
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

    public boolean isProduceLatencyFaultEnable() {
        return produceLatencyFaultEnable;
    }

    public void setProduceLatencyFaultEnable(boolean produceLatencyFaultEnable) {
        this.produceLatencyFaultEnable = produceLatencyFaultEnable;
    }

    public int getProduceMessageSize() {
        return produceMessageSize;
    }

    public void setProduceMessageSize(int produceMessageSize) {
        this.produceMessageSize = produceMessageSize;
    }

    public Map<String, RocketMqTransferConsumerProperties> getConsumers() {
        return consumers;
    }

    public void setConsumers(Map<String, RocketMqTransferConsumerProperties> consumers) {
        this.consumers.putAll(consumers);
    }

    /**
     * RocketMQ Transfer ConsumerProperty
     *
     * @author svnee
     */
    public static class RocketMqTransferConsumerProperties {

        /**
         * 消费者组
         */
        private String consumerGroup;

        /**
         * topic
         */
        private String topic = "easyevent";

        /**
         * tags
         * 例如：tag1,tag2,tag3
         */
        private String tags = "*";

        /**
         * 最大消费者数
         */
        private Integer consumerMaxThread = 3;

        /**
         * 最小消费者数
         */
        private Integer consumerMinThread = 1;

        /**
         * 最大消费并发最大span
         */
        private Integer consumeConcurrentlyMaxSpan = 10;

        /**
         * 消费消息最大重试次数
         */
        private Integer consumeMaxRetry = 5;

        /**
         * 消费重试最大时间间隔 单位：秒
         */
        private Integer consumeRetryDelayTimeIntervalSeconds = 5;

        /**
         * 消费重试限流时间，单位：s
         */
        private Integer consumeLimingRetryDelayTimeBaseSeconds = 5;

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

        public String getTags() {
            return tags;
        }

        public void setTags(String tags) {
            this.tags = tags;
        }

        public Integer getConsumerMaxThread() {
            return consumerMaxThread;
        }

        public void setConsumerMaxThread(Integer consumerMaxThread) {
            this.consumerMaxThread = consumerMaxThread;
        }

        public Integer getConsumerMinThread() {
            return consumerMinThread;
        }

        public void setConsumerMinThread(Integer consumerMinThread) {
            this.consumerMinThread = consumerMinThread;
        }

        public Integer getConsumeConcurrentlyMaxSpan() {
            return consumeConcurrentlyMaxSpan;
        }

        public void setConsumeConcurrentlyMaxSpan(Integer consumeConcurrentlyMaxSpan) {
            this.consumeConcurrentlyMaxSpan = consumeConcurrentlyMaxSpan;
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

        public Integer getConsumeLimingRetryDelayTimeBaseSeconds() {
            return consumeLimingRetryDelayTimeBaseSeconds;
        }

        public void setConsumeLimingRetryDelayTimeBaseSeconds(Integer consumeLimingRetryDelayTimeBaseSeconds) {
            this.consumeLimingRetryDelayTimeBaseSeconds = consumeLimingRetryDelayTimeBaseSeconds;
        }

        @Override
        public String toString() {
            return "RocketMqTransferConsumerProperties{" +
                "consumerGroup='" + consumerGroup + '\'' +
                ", topic='" + topic + '\'' +
                ", tags='" + tags + '\'' +
                ", consumerMaxThread=" + consumerMaxThread +
                ", consumerMinThread=" + consumerMinThread +
                ", consumeConcurrentlyMaxSpan=" + consumeConcurrentlyMaxSpan +
                ", consumeMaxRetry=" + consumeMaxRetry +
                ", consumeRetryDelayTimeIntervalSeconds=" + consumeRetryDelayTimeIntervalSeconds +
                ", consumeLimingRetryDelayTimeBaseSeconds=" + consumeLimingRetryDelayTimeBaseSeconds +
                '}';
        }
    }

    @Override
    public String toString() {
        return "RocketTransferProperties{" +
            "host='" + host + '\'' +
            ", produceGroup='" + produceGroup + '\'' +
            ", produceTimeout=" + produceTimeout +
            ", produceTryTimes=" + produceTryTimes +
            ", produceLatencyFaultEnable=" + produceLatencyFaultEnable +
            ", produceMessageSize=" + produceMessageSize +
            ", consumers=" + consumers +
            '}';
    }
}
