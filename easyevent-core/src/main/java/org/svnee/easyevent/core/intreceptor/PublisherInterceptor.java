package org.svnee.easyevent.core.intreceptor;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * 同步拦截器
 *
 * @author svnee
 */
public interface PublisherInterceptor {

    /**
     * 默认先拦截顺序
     *
     * @return 顺序
     */
    default int order() {
        return Integer.MAX_VALUE;
    }

    /**
     * 发布开始之前
     *
     * @param event event
     * @param context 上下文
     * @return true-执行下一个拦截器，否则默认已经响应完成。直接返回
     */
    default boolean prePublish(Object event, PublisherInterceptorContext context) {
        return true;
    }

    /**
     * 发布完成后
     *
     * @param event event
     * @param context context
     * @param ex 发生异常时的异常信息
     */
    default void afterCompletion(Object event, PublisherInterceptorContext context, @Nullable Exception ex) {

    }

}
