package org.svnee.easyevent.transfer.rocket.property;

import lombok.Data;

/**
 * RocketMQ Sender Properties
 *
 * @author svnee
 **/
@Data
public class RocketMqCommonProperty {

    /**
     * host
     */
    private String host = "127.0.0.1:9876";

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
}
