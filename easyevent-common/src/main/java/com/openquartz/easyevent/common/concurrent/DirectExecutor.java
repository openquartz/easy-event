package com.openquartz.easyevent.common.concurrent;

import com.alibaba.ttl.threadpool.TtlExecutors;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An {@link Executor} that runs each task in the thread that invokes {@link Executor#execute
 * execute}.
 *
 * @author svnee
 */
public enum DirectExecutor implements ExecutorService {
    INSTANCE;

    private static final ExecutorService TRACE_THREAD_POOL = TtlExecutors.getTtlExecutorService(new TraceThreadPoolExecutor(2, 2, 60,
        TimeUnit.SECONDS, new ArrayBlockingQueue<>(1024), new CallerRunsPolicy()));

    @Override
    public void execute(Runnable command) {
        assert TRACE_THREAD_POOL != null;
        TRACE_THREAD_POOL.execute(command);
    }

    @Override
    public void shutdown() {
        assert TRACE_THREAD_POOL != null;
        TRACE_THREAD_POOL.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        assert TRACE_THREAD_POOL != null;
        return TRACE_THREAD_POOL.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        assert TRACE_THREAD_POOL != null;
        return TRACE_THREAD_POOL.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        assert TRACE_THREAD_POOL != null;
        return TRACE_THREAD_POOL.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        assert TRACE_THREAD_POOL != null;
        return TRACE_THREAD_POOL.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        assert TRACE_THREAD_POOL != null;
        return TRACE_THREAD_POOL.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        assert TRACE_THREAD_POOL != null;
        return TRACE_THREAD_POOL.submit(task, result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        assert TRACE_THREAD_POOL != null;
        return TRACE_THREAD_POOL.submit(task);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        assert TRACE_THREAD_POOL != null;
        return TRACE_THREAD_POOL.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
        throws InterruptedException {
        assert TRACE_THREAD_POOL != null;
        return TRACE_THREAD_POOL.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        assert TRACE_THREAD_POOL != null;
        return TRACE_THREAD_POOL.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        assert TRACE_THREAD_POOL != null;
        return TRACE_THREAD_POOL.invokeAny(tasks, timeout, unit);
    }
}
