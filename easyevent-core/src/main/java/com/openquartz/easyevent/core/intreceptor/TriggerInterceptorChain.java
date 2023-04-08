package com.openquartz.easyevent.core.intreceptor;

import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import com.openquartz.easyevent.common.utils.CollectionUtils;
import com.openquartz.easyevent.transfer.api.message.EventMessage;

/**
 * TriggerInterceptorChain
 *
 * @author svnee
 **/
@Slf4j
public final class TriggerInterceptorChain {

    private TriggerInterceptorChain() {
    }

    private static final List<TriggerInterceptor> INTERCEPTOR_LIST = new ArrayList<>();

    public static boolean applyPreTrigger(EventMessage message, TriggerInterceptorContext context) {
        int endIndex = 0;
        if (CollectionUtils.isNotEmpty(INTERCEPTOR_LIST)) {
            for (TriggerInterceptor interceptor : INTERCEPTOR_LIST) {
                endIndex++;
                if (!interceptor.preTrigger(message, context)) {
                    triggerAfterCompletion(message, context, endIndex, null);
                    return false;
                }
            }
        }
        return true;
    }

    public static void addInterceptor(TriggerInterceptor interceptor) {

        checkNotNull(interceptor);

        INTERCEPTOR_LIST.add(interceptor);
        INTERCEPTOR_LIST.sort(Comparator.comparingInt(TriggerInterceptor::order));
    }

    /**
     * 执行
     *
     * @param message 事件
     * @param context 发布事件上下文
     * @param endIndex 结束下标
     * @param ex ex
     */
    private static void triggerAfterCompletion(EventMessage message, TriggerInterceptorContext context,
        Integer endIndex,
        @Nullable Exception ex) {

        List<TriggerInterceptor> interceptors = INTERCEPTOR_LIST;
        if (CollectionUtils.isNotEmpty(interceptors)) {
            for (int i = endIndex; i > 0; i--) {
                TriggerInterceptor interceptor = interceptors.get(i - 1);
                try {
                    interceptor.afterCompletion(message, context, ex);
                } catch (Throwable ex2) {
                    log.error("HandlerInterceptor.afterCompletion threw exception", ex2);
                }
            }
        }
    }

    /**
     * 执行
     *
     * @param message trigger-message
     * @param context 发布事件上下文
     * @param ex ex
     */
    public static void triggerAfterCompletion(EventMessage message, TriggerInterceptorContext context,
        @Nullable Exception ex) {
        triggerAfterCompletion(message, context, INTERCEPTOR_LIST.size(), ex);
    }
}
