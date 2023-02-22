package org.svnee.easyevent.core.intreceptor;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.svnee.easyevent.transfer.api.message.EventMessage;

/**
 * Trigger Interceptor
 *
 * @author svnee
 */
public interface TriggerInterceptor {

    /**
     * 默认先拦截顺序
     *
     * @return 顺序
     */
    default int order() {
        return Integer.MAX_VALUE;
    }

    /**
     * 处理开始之前
     *
     * @param message trigger-消息
     * @param context context
     * @return trigger flag
     */
    default boolean preTrigger(EventMessage message, TriggerInterceptorContext context) {
        return true;
    }

    /**
     * 处理完成后
     *
     * @param message message
     * @param context context
     * @param ex 发生异常时的异常信息
     */
    default void afterCompletion(EventMessage message, TriggerInterceptorContext context, @Nullable Exception ex) {

    }
}
