package com.openquartz.easyevent.transfer.disruptor.factory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * DisruptorTriggerThreadFactory
 *
 * @author svnee
 */
public final class DisruptorTriggerThreadFactory implements ThreadFactory {

    private final boolean daemon;

    private final ThreadGroup threadGroup = new ThreadGroup("EasyEventDisruptorTrigger");

    private final AtomicLong threadNumber = new AtomicLong(1);

    private final String namePrefix;

    private DisruptorTriggerThreadFactory(final String namePrefix, final boolean daemon) {
        this.namePrefix = namePrefix;
        this.daemon = daemon;
    }

    /**
     * create ThreadFactory.
     *
     * @param namePrefix namePrefix
     * @param daemon daemon
     * @return ThreadFactory thread factory
     */
    public static ThreadFactory create(final String namePrefix, final boolean daemon) {
        return new DisruptorTriggerThreadFactory(namePrefix, daemon);
    }

    @Override
    public Thread newThread(final Runnable runnable) {
        Thread thread = new Thread(threadGroup, runnable,
            threadGroup.getName() + "-" + namePrefix + "-" + threadNumber.getAndIncrement());
        thread.setDaemon(daemon);
        if (thread.getPriority() != Thread.NORM_PRIORITY) {
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        return thread;
    }
}
