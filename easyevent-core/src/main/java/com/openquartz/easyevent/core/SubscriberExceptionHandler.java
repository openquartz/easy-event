package com.openquartz.easyevent.core;

/**
 * Handler for exceptions thrown by event subscribers.
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
