package org.svnee.easyevent.transfer.rocket.property;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;
import org.svnee.easyevent.common.constant.CommonConstants;
import org.svnee.easyevent.common.utils.StringUtils;

/**
 * RocketMqTriggerProperty
 *
 * @author svnee
 **/
@Data
public class RocketMqConsumerProperty {

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
        if (StringUtils.isBlank(tags) || CommonConstants.ALL_EXPRESSION.equals(tags)) {
            return CommonConstants.ALL_EXPRESSION;
        }
        return Stream.of(tags.trim().split(CommonConstants.COMMA))
            .map(String::trim)
            .collect(Collectors.joining(CommonConstants.ROCKETMQ_SUB_EXPRESSION_SPLITTER));
    }

}
