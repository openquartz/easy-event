package com.openquartz.easyevent.storage.exception;

import com.openquartz.easyevent.common.exception.EasyEventErrorCode;

/**
 * IdentifyErrorCode
 * @author svnee
 */
public enum IdentifyErrorCode implements EasyEventErrorCode {

    ENABLE_SHARDING_MUST_PROVIDER_IDENTIFY_GENERATOR("01","enable sharding must provider IdGenerator!"),
    SHARDING_PROPERTY_ILLEGAL_ERROR("02","sharding property must not be 0,currentVal:{0}",true),
    ;
    private final String errorCode;
    private final String errorMsg;
    private final boolean replacePlaceHold;

    private static final String SIMPLE_BASE_CODE = "IdentifyError-";

    IdentifyErrorCode(String errorCode, String errorMsg) {
        this(errorCode, errorMsg, false);
    }

    IdentifyErrorCode(String errorCode, String errorMsg, boolean replacePlaceHold) {
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
