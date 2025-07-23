package com.openquartz.easyevent.core;

/**
 * Handler for exceptions thrown by event subscribers.
 *
 * 源代码链接自：Google Guava (com.google.common.eventbus.SubscriberExceptionHandler)
 * <link>https://github.com/google/guava/blob/master/guava/src/com/google/common/eventbus/SubscriberExceptionHandler.java</link>
 *
 *
 * @author svnee
 * @since 1.1.0
 */
public interface SubscriberExceptionHandler {

    /**
     * Handles exceptions thrown by subscribers.
     *
     * @param exception handle exception
     * @param context subscriber context
     */
    void handleException(Throwable exception, SubscriberExceptionContext context);

}
