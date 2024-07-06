package com.openquartz.easyevent.common.exception;

import java.text.MessageFormat;

/**
 * Event事件异常
 *
 * @author svnee
 */
public class EasyEventException extends RuntimeException {

    private final transient EasyEventErrorCode errorCode;

    public EasyEventException(EasyEventErrorCode errorCode) {
        super(errorCode.getErrorMsg());
        this.errorCode = errorCode;
    }

    public EasyEventException(EasyEventErrorCode errorCode, String errorMsg) {
        super(errorMsg);
        this.errorCode = errorCode;
    }

    public EasyEventErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * 替换占位符号
     *
     * @param placeHold 占位
     * @return 异常
     */
    public static EasyEventException replacePlaceHold(EasyEventErrorCode errorCode, Object... placeHold) {
        return new EasyEventException(errorCode, MessageFormat.format(errorCode.getErrorMsg(), placeHold));
    }

//    @Override
//    public synchronized Throwable fillInStackTrace() {
//        return this;
//    }
}
