package com.openquartz.easyevent.core;

import com.openquartz.easyevent.common.utils.ExceptionUtils;

/**
 * 直接中断
 * @author svnee
 **/
public class DirectInterruptExceptionHandler implements SubscriberExceptionHandler {

    @Override
    public void handleException(Throwable exception, SubscriberExceptionContext context) {
        ExceptionUtils.rethrow(exception);
    }
}
