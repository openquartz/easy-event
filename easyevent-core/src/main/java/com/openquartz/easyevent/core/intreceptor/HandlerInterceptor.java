package com.openquartz.easyevent.core.intreceptor;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Handle Interceptor
 *
 * @author svnee
 */
public interface HandlerInterceptor<T> {

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
     * @param event event
     * @param handler 处理器
     * @param context 上下文
     * @return true-执行下一个拦截器，否则默认已经响应完成。直接返回
     */
    default boolean preHandle(T event, Object handler, HandlerInterceptorContext context) {
        return true;
    }

    /**
     * 处理完成后
     *
     * @param event event
     * @param handler 处理器
     * @param context context
     * @param ex 发生异常时的异常信息
     */
    default void afterCompletion(T event, Object handler, HandlerInterceptorContext context,
        @Nullable Exception ex) {

    }


}
