package com.openquartz.easyevent.core.intreceptor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import com.openquartz.easyevent.common.utils.GenericUtils;

/**
 * HandlerInterceptorCenter
 *
 * @author svnee
 **/
public final class HandlerInterceptorCenter {

    private HandlerInterceptorCenter() {
    }

    private static final Map<Class<?>, List<HandlerInterceptor<?>>> INTERCEPTOR_MAP = new ConcurrentHashMap<>();

    /**
     * add interceptor
     *
     * @param interceptor interceptor
     * @param realClass interceptor real-class
     */
    public static synchronized void addInterceptor(HandlerInterceptor<?> interceptor, Class<?> realClass) {

        Class<?>[] parameterType = GenericUtils
            .getGenericInterfaceParameter(realClass, HandlerInterceptor.class);
        Class<?> eventType = parameterType[0];

        List<HandlerInterceptor<?>> interceptorList = INTERCEPTOR_MAP.getOrDefault(eventType, new ArrayList<>());
        interceptorList.add(interceptor);
        INTERCEPTOR_MAP.put(eventType, interceptorList);
    }

    /**
     * match event-type interceptor
     *
     * @param eventType event-type
     * @return interceptor
     */
    public static List<HandlerInterceptor> match(Class<?> eventType) {
        List<HandlerInterceptor> interceptorList = new ArrayList<>();
        for (Entry<Class<?>, List<HandlerInterceptor<?>>> entry : INTERCEPTOR_MAP.entrySet()) {
            if (Objects.equals(entry.getKey(), eventType) || entry.getKey().isAssignableFrom(eventType)) {
                interceptorList.addAll(entry.getValue());
            }
        }
        interceptorList.sort((Comparator.comparingInt(HandlerInterceptor::order)));
        return interceptorList;
    }

}
