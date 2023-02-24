package org.svnee.easyevent.transfer.kafka.common;

import java.util.Map;
import lombok.Data;

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
    }

}
