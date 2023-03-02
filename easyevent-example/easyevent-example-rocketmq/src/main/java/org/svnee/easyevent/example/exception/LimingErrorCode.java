package org.svnee.easyevent.example.exception;

import org.svnee.easyevent.common.exception.EasyEventErrorCode;

/**
 * @author svnee
 **/
public enum LimingErrorCode implements EasyEventErrorCode {

    CONSUME_LIMITING_BLOCKED_ERROR("01", "consume limiting blocked error!"),
    ;
    private final String errorCode;
    private final String errorMsg;
    private final boolean replacePlaceHold;

    private static final String SIMPLE_BASE_CODE = "LimitingError-";

    LimingErrorCode(String errorCode, String errorMsg) {
        this(errorCode, errorMsg, false);
    }

    LimingErrorCode(String errorCode, String errorMsg, boolean replacePlaceHold) {
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
