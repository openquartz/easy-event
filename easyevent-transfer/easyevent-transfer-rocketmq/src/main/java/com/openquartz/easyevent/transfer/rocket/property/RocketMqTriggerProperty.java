package com.openquartz.easyevent.transfer.rocket.property;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;
import com.openquartz.easyevent.common.constant.CommonConstants;
import com.openquartz.easyevent.common.utils.StringUtils;

/**
 * RocketMQ Trigger 配置
 *
 * @author svnee
 **/
@Data
public class RocketMqTriggerProperty {

    /**
     * rocketmq-consumer-property
     */
    private Map<String, RocketMqConsumerProperty> consumerPropertyMap;

    /**
     * single rocketmq consumer property
     */
    public static class RocketMqConsumerProperty {

        /**
         * 消费者组
         */
        private String consumerGroup;

        /**
         * topic
         */
        private String topic = "";

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

        /**
         * 获取消费者tag子表达式
         *
         * @return 订阅者表达式
         */
        public String getConsumerTagSubExpression() {
            if (StringUtils.isBlank(tags) || CommonConstants.ALL_MATCH_EXPRESSION.equals(tags)) {
                return CommonConstants.ALL_MATCH_EXPRESSION;
            }
            return Stream.of(tags.trim().split(CommonConstants.COMMA))
                .map(String::trim)
                .collect(Collectors.joining(CommonConstants.ROCKETMQ_SUB_EXPRESSION_SPLITTER));
        }

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
            return "RocketMqConsumerProperty{" +
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

}
