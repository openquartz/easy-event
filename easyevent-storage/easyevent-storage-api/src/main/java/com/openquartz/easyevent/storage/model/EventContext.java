package com.openquartz.easyevent.storage.model;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.Getter;

/**
 * EventContext
 *
 * @author svnee
 */
@Getter
public class EventContext {

    private boolean soaFilter = false;

    public synchronized void setSoaFilter(boolean soaFilter) {
        this.soaFilter = soaFilter;
    }

    private static final ThreadLocal<EventContext> THREAD_LOCAL;

    static {
        THREAD_LOCAL = TransmittableThreadLocal.withInitial(EventContext::new);
    }

    public static EventContext get() {
        return THREAD_LOCAL.get();
    }

    public static void set(EventContext eventContext) {
        THREAD_LOCAL.set(eventContext);
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }
}
