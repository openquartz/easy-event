package com.openquartz.easyevent.common.exception;

import lombok.Getter;

/**
 * @author svnee
 **/
@Getter
public enum CommonErrorCode implements EasyEventErrorCode {

    PARAM_ILLEGAL_ERROR("01", "参数不合法异常"),
    CLASS_NOT_FOUND_ERROR("02", "Class {0} not exist!"),
    THREAD_PRIORITY_SETTER_MORE_ERROR("03", "Thread priority ({0}) must be >= {1}", true),
    THREAD_PRIORITY_SETTER_LESS_THAN_ERROR("04", "Thread priority ({0}) must be <= {1}", true),
    THREAD_EXECUTE_EXCEPTION_NULLABLE_ERROR("05", "Handle Exception must not be null!"),
    THREAD_POOL_FACTORY_NULLABLE_ERROR("06", "ThreadPoolFactory must not be null!"),
    METHOD_NOT_EXIST_ERROR("07", "Method not exist"),
    FILE_NOT_EXIST_ERROR("08", "file not exist error!"),
    PROPERTY_NOT_EXIST_ERROR("09", "Property Not Exist!"),
    CANNOT_GET_LOCK_ERROR("10", "Cannot Acquire Lock!"),
    ;
    private final String errorCode;
    private final String errorMsg;
    private final boolean replacePlaceHold;

    private static final String SIMPLE_BASE_CODE = "CommonError-";

    CommonErrorCode(String errorCode, String errorMsg) {
        this(errorCode, errorMsg, false);
    }

    CommonErrorCode(String errorCode, String errorMsg, boolean replacePlaceHold) {
        this.errorCode = SIMPLE_BASE_CODE + errorCode;
        this.errorMsg = errorMsg;
        this.replacePlaceHold = replacePlaceHold;
    }
}
