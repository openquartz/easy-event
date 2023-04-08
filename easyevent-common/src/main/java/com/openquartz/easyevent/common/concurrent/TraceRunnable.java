package com.openquartz.easyevent.common.concurrent;

import java.util.Map;
import java.util.Objects;

/**
 * Trace 线程
 *
 * @author svne
 */
public class TraceRunnable implements Runnable {

    private Map<String, Object> traceContextMap;

    private final Runnable runnable;

    public Map<String, Object> getTraceContextMap() {
        return traceContextMap;
    }

    public void setTraceContextMap(Map<String, Object> traceContextMap) {
        this.traceContextMap = traceContextMap;
    }

    public void clear() {
        if (Objects.nonNull(traceContextMap)) {
            traceContextMap.clear();
        }
    }

    public TraceRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void run() {
        runnable.run();
    }
}
