package com.openquartz.easyevent.transfer.rabbitmq.exception;

import com.openquartz.easyevent.common.exception.EasyEventErrorCode;
import lombok.Getter;

/**
 * RabbitMQ Transfer Error Code
 *
 * @author svnee
 **/
@Getter
public enum RabbitMqTransferErrorCode implements EasyEventErrorCode {

    /**
     * 发送分区超出边界
     */
    THE_SEND_PARTITION_OUT_OF_BOUNDS("RABBITMQ-001", "the send partition out of bounds! topic:{0},min:{1},max:{2},current:{3}"),

    /**
     * 发送者失败
     */
    SENDER_FAILED("RABBITMQ-002", "sender failed! msgId:{0},exchange:{1},routingKey:{2}"),

    /**
     * 消费者队列配置非法
     */
    CONSUMER_QUEUE_CONFIG_ILLEGAL("RABBITMQ-003", "consumer queue config illegal! queue:{0}"),

    ;

    private final String errorCode;
    private final String errorMsg;

    RabbitMqTransferErrorCode(String errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }
}