package com.openquartz.easyevent.core.exception;

import lombok.Getter;
import com.openquartz.easyevent.common.exception.EasyEventErrorCode;

/**
 * EventBusErrorCode
 *
 * @author svnee
 */
@Getter
public enum EventBusErrorCode implements EasyEventErrorCode {

    EVENT_HANDLE_ERROR("01", "Event Subscriber handle error!"),
    SUBSCRIBER_METHOD_ARGS_NUM_ERROR("02",
        "Method {0} has @Subscribe annotation but has {1} parameters.Subscriber methods must have exactly 1 parameter.",
        true),
    SUBSCRIBER_METHOD_ARGS_POSITIVE_ERROR("03",
        "@Subscribe method {0}'s parameter is {1}.Subscriber methods cannot accept primitives. Consider changing the parameter to wrapped object.",
        true),
    ;
    private final String errorCode;
    private final String errorMsg;
    private final boolean replacePlaceHold;

    private static final String SIMPLE_BASE_CODE = "EventBusError-";

    EventBusErrorCode(String errorCode, String errorMsg) {
        this(errorCode, errorMsg, false);
    }

    EventBusErrorCode(String errorCode, String errorMsg, boolean replacePlaceHold) {
        this.errorCode = SIMPLE_BASE_CODE + errorCode;
        this.errorMsg = errorMsg;
        this.replacePlaceHold = replacePlaceHold;
    }
}
