package com.openquartz.easyevent.starter.rocketmq.spring.boot.autoconfig;

import java.util.Map;
import java.util.TreeMap;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RocketMQ transfer 配置
 *
 * @author svnee
 **/
@Getter
@Slf4j
@ConfigurationProperties(prefix = RocketTransferProperties.PREFIX)
public class RocketTransferProperties {

    public static final String PREFIX = "easyevent.transfer.trigger.rocketmq";

    /**
     * host
     */
    @Setter
    private String host = "127.0.0.1:9876";

    /**
     * 发送者组
     */
    @Setter
    public String produceGroup = "EasyEvent";

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
     * 故障轉移
     */
    @Setter
    private boolean produceLatencyFaultEnable = true;

    /**
     * produce message size
     */
    @Setter
    private int produceMessageSize = 1000 * 1000;

    /**
     * consumers
     */
    private final Map<String, RocketMqTransferConsumerProperties> consumers = new TreeMap<>(
        String.CASE_INSENSITIVE_ORDER);

    public void setConsumers(Map<String, RocketMqTransferConsumerProperties> consumers) {
        this.consumers.putAll(consumers);
    }

    /**
     * RocketMQ Transfer ConsumerProperty
     *
     * @author svnee
     */
    @Data
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
