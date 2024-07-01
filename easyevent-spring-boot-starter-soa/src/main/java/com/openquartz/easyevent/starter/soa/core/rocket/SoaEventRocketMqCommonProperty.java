package com.openquartz.easyevent.starter.soa.core.rocket;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "easyevent.soa.event-center.rocketmq")
public class SoaEventRocketMqCommonProperty {

    /**
     * host
     */
    private String host = "127.0.0.1:9876";

    /**
     * event-center 事件中心topic
     */
    private String topic = "event_center";

    /**
     * produce-group
     */
    private String produceGroup;

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
     * 发送消息大小限制 单位：byte
     */
    private int produceMessageSize = 1000 * 1000;

    /**
     * consume-group
     */
    private String consumeGroup;

    /**
     * 消费线程最小数量
     */
    private Integer consumeMinThreadNums = 1;

    /**
     * 消费线程最大数量
     */
    private Integer consumeMaxThreadNums = 10;

    /**
     * 消费线程最大数量
     */
    private int consumeConcurrentlyMaxSpan = 10;

    /**
     * 消费最大重试次数
     */
    private int consumeMaxRetry = 3;

    /**
     * 消费延迟时间间隔
     */
    private int consumeLimingRetryDelayTimeBaseSeconds = 10;

    /**
     * 重试间隔
     */
    private int consumeRetryDelayTimeIntervalSeconds = 10;
}
