package com.openquartz.easyevent.core.intreceptor;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import com.openquartz.easyevent.common.utils.CollectionUtils;

/**
 * 处理器拦截链路
 *
 * @author svnee
 **/
@Slf4j
@SuppressWarnings("all")
public class HandlerInterceptorChain {

    /**
     * 处理器
     */
    private final Object handler;

    /**
     * event-type
     */
    private final Class<?> eventType;

    /**
     * 拦截器
     */
    private final List<HandlerInterceptor> interceptorList;

    public HandlerInterceptorChain(Object handler, Class<?> eventType) {
        this.handler = handler;
        this.eventType = eventType;
        this.interceptorList = HandlerInterceptorCenter.match(eventType);
    }

    public Object getHandler() {
        return handler;
    }

    public Class<?> getEventType() {
        return eventType;
    }

    public List<HandlerInterceptor> getInterceptorList() {
        return interceptorList;
    }

    public boolean applyPreHandle(Object event, HandlerInterceptorContext context) {
        int endIndex = 0;
        if (CollectionUtils.isNotEmpty(interceptorList)) {
            for (HandlerInterceptor interceptor : interceptorList) {
                endIndex++;
                if (!interceptor.preHandle(event, this.handler, context)) {
                    triggerAfterCompletion(event, context, endIndex, null);
                    return false;
                }
            }
        }
        return true;
    }

    public void triggerAfterCompletion(Object event, HandlerInterceptorContext context, Integer endIndex,
        @Nullable Exception ex) {

        List<HandlerInterceptor> interceptors = getInterceptorList();
        if (CollectionUtils.isNotEmpty(interceptors)) {
            for (int i = endIndex; i > 0; i--) {
                HandlerInterceptor interceptor = interceptors.get(i - 1);
                try {
                    interceptor.afterCompletion(event, this.handler, context, ex);
                } catch (Throwable ex2) {
                    log.error("HandlerInterceptor.afterCompletion threw exception", ex2);
                }
            }
        }
    }

}
