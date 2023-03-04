package org.svnee.easyevent.core.intreceptor;

import static org.svnee.easyevent.common.utils.ParamUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.svnee.easyevent.common.utils.CollectionUtils;

/**
 * 发布者拦截链
 *
 * @author svnee
 **/
@Slf4j
public final class PublisherInterceptorChain {

    private PublisherInterceptorChain() {
    }

    private static final List<PublisherInterceptor> INTERCEPTOR_LIST = new ArrayList<>();

    public static boolean applyPrePublish(Object event, PublisherInterceptorContext context) {
        int endIndex = 0;
        if (CollectionUtils.isNotEmpty(INTERCEPTOR_LIST)) {
            for (PublisherInterceptor interceptor : INTERCEPTOR_LIST) {
                endIndex++;
                if (!interceptor.prePublish(event, context)) {
                    triggerAfterCompletion(event, context, endIndex, null);
                    return false;
                }
            }
        }
        return true;
    }

    public static void addInterceptor(PublisherInterceptor interceptor){

        checkNotNull(interceptor);

        INTERCEPTOR_LIST.add(interceptor);
        INTERCEPTOR_LIST.sort(Comparator.comparingInt(PublisherInterceptor::order));
    }

    /**
     * 执行
     *
     * @param event 事件
     * @param context 发布事件上下文
     * @param endIndex 结束下标
     * @param ex ex
     */
    private static void triggerAfterCompletion(Object event, PublisherInterceptorContext context, Integer endIndex,
        @Nullable Exception ex) {

        List<PublisherInterceptor> interceptors = INTERCEPTOR_LIST;
        if (CollectionUtils.isNotEmpty(interceptors)) {
            for (int i = endIndex; i > 0; i--) {
                PublisherInterceptor interceptor = interceptors.get(i - 1);
                try {
                    interceptor.afterCompletion(event, context, ex);
                } catch (Exception ex2) {
                    log.error("HandlerInterceptor.afterCompletion threw exception", ex2);
                }
            }
        }
    }

    /**
     * 执行
     *
     * @param event 事件
     * @param context 发布事件上下文
     * @param ex ex
     */
    public static void triggerAfterCompletion(Object event, PublisherInterceptorContext context,
        @Nullable Exception ex) {
        triggerAfterCompletion(event, context, INTERCEPTOR_LIST.size(), ex);
    }

}
