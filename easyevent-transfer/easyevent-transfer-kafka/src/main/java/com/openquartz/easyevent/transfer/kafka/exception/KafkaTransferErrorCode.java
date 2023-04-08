package com.openquartz.easyevent.transfer.kafka.exception;

import com.openquartz.easyevent.common.exception.EasyEventErrorCode;

/**
 * KafkaTransferErrorCode
 *
 * @author svnee
 */
public enum KafkaTransferErrorCode implements EasyEventErrorCode {

    THE_SEND_PARTITION_OUT_OF_BOUNDS("01", "the topic:{0},partition outOfBounds!range[{1},{2}],current:{3}", true),
    ;
    private final String errorCode;
    private final String errorMsg;
    private final boolean replacePlaceHold;

    private static final String SIMPLE_BASE_CODE = "KafkaTransferError-";

    KafkaTransferErrorCode(String errorCode, String errorMsg) {
        this(errorCode, errorMsg, false);
    }

    KafkaTransferErrorCode(String errorCode, String errorMsg, boolean replacePlaceHold) {
        this.errorCode = SIMPLE_BASE_CODE + errorCode;
        this.errorMsg = errorMsg;
        this.replacePlaceHold = replacePlaceHold;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorMsg() {
        return errorMsg;
    }

    @Override
    public boolean isReplacePlaceHold() {
        return replacePlaceHold;
    }
}
