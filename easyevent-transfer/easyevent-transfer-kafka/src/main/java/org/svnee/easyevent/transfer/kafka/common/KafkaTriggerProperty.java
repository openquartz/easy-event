package org.svnee.easyevent.transfer.kafka.common;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;
import org.svnee.easyevent.common.constant.CommonConstants;
import org.svnee.easyevent.common.utils.StringUtils;

/**
 * kafka Trigger 配置
 *
 * @author svnee
 **/
@Data
public class KafkaTriggerProperty {

    /**
     * rocketmq-consumer-property
     */
    private Map<String, KafkaConsumerProperty> consumerPropertyMap;

    /**
     * single kafka consumer property
     */
    public static class KafkaConsumerProperty {

        /**
         * 消费者组
         */
        private String consumerGroup;

        /**
         * topic
         */
        private String topic = "";

        /**
         * partition
         */
        private String partition = "*";

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

        public String getPartition() {
            return partition;
        }

        public void setPartition(String partition) {
            this.partition = partition;
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
    }

}
