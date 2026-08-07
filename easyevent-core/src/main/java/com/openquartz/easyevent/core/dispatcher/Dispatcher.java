
package com.openquartz.easyevent.core.dispatcher;

import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotNull;

import com.openquartz.easyevent.core.Subscriber;
import com.openquartz.easyevent.core.Subscriber.SynchronizedSubscriber;
import com.openquartz.easyevent.core.expression.ExpressionParser;
import com.openquartz.easyevent.core.intreceptor.HandlerInterceptorContext;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Future;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import com.openquartz.easyevent.common.model.Pair;
import com.openquartz.easyevent.common.utils.CollectionUtils;

/**
 * Dispatcher
 *
 * 源代码链接自：Google Guava (com.google.common.eventbus.Dispatcher)
 * <link>https://github.com/google/guava/blob/master/guava/src/com/google/common/eventbus/Dispatcher.java</link>
 *
 * @author svnee
 */
@Slf4j
public abstract class Dispatcher {

    private final ExpressionParser expressionParser;

    protected Dispatcher(ExpressionParser expressionParser) {
        this.expressionParser = expressionParser;
    }

    /**
     * Returns a dispatcher that queues events that are posted reentrantly on a thread that is already
     * dispatching an event, guaranteeing that all events posted on a single thread are dispatched to
     * all subscribers in the order they are posted.
     *
     * <p>When all subscribers are dispatched to using a <i>direct</i> executor (which dispatches on
     * the same thread that posts the event), this yields a breadth-first dispatch order on each
     * thread. That is, all subscribers to a single event A will be called before any subscribers to
     * any events B and C that are posted to the event bus by the subscribers to A.
     */
    public static Dispatcher perThreadDispatchQueue(ExpressionParser expressionParser) {
        return new PerThreadQueuedDispatcher(expressionParser);
    }

    /**
     * Returns a dispatcher that dispatches events to subscribers immediately as they're posted
     * without using an intermediate queue to change the dispatch order. This is effectively a
     * depth-first dispatch order, vs. breadth-first when using a queue.
     */
    public static Dispatcher immediate(ExpressionParser expressionParser) {
        return new ImmediateDispatcher(expressionParser);
    }

    /**
     * Returns a dispatcher that dispatches events to subscribers immediately as they're posted
     * without waiting for the concurrent execution result.
     */
    public static Dispatcher asyncImmediate(ExpressionParser expressionParser) {
        return new AsyncImmediateDispatcher(expressionParser);
    }

    /**
     * Dispatches the given {@code event} to the given {@code subscribers}.
     */
    public abstract DispatchInvokeResult dispatch(Object event, Iterator<Subscriber> subscribers, boolean joinTransaction);

    protected DispatchInvokeResult dispatchConcurrentEvent(
            List<Subscriber> concurrentSubscriberList,
            Object event,
            HandlerInterceptorContext context) {

        DispatchInvokeResult invokeResult = new DispatchInvokeResult(event);

        if (CollectionUtils.isEmpty(concurrentSubscriberList)) {
            return invokeResult;
        }

        List<Pair<Future<Boolean>, Subscriber>> dispatchFutureList = new ArrayList<>();
        for (Subscriber subscriber : concurrentSubscriberList) {

            if (!subscriber.shouldSubscribe(expressionParser, event)) {
                continue;
            }

            Future<Boolean> dispatchFuture = subscriber.concurrentDispatchEvent(event, context);
            dispatchFutureList.add(Pair.of(dispatchFuture, subscriber));
        }
        for (Pair<Future<Boolean>, Subscriber> pair : dispatchFutureList) {
            try {
                Boolean success = pair.getLeft().get();
                if (Boolean.TRUE.equals(success)) {
                    invokeResult.addSubscriber(pair.getRight());
                }
            } catch (InterruptedException ex) {
                invokeResult.setInvokeError(ex);
                log.error("[Dispatcher#dispatchConcurrentEvent] event:{},subscriber:{} invoke-error!", event,
                        pair.getRight().getTargetIdentify(), ex);
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                invokeResult.setInvokeError(ex);
                log.error("[Dispatcher#dispatchConcurrentEvent] event:{},subscriber:{} invoke-error!", event,
                        pair.getRight().getTargetIdentify(), ex);
            }
        }
        return invokeResult;
    }

    protected DispatchInvokeResult dispatchSyncEvent(List<Subscriber> syncSubscriberList,
                                                     Object event,
                                                     HandlerInterceptorContext context) {
        DispatchInvokeResult invokeResult = new DispatchInvokeResult(event);

        if (CollectionUtils.isEmpty(syncSubscriberList)) {
            return invokeResult;
        }

        for (Subscriber subscriber : syncSubscriberList) {
            try {

                if (!subscriber.shouldSubscribe(expressionParser, event)) {
                    continue;
                }

                boolean success = subscriber.singleDispatchEvent(event, context);
                if (success) {
                    invokeResult.addSubscriber(subscriber);
                }
            } catch (Exception ex) {
                invokeResult.setInvokeError(ex);
                log.error("[Dispatcher#dispatchSyncEvent] event:{},subscriber:{} invoke-error!", event,
                        subscriber.getTargetIdentify(), ex);
                // Keep already-successful subscribers so retries can skip them.
                return invokeResult;
            }
        }
        return invokeResult;
    }

    /**
     * Partition subscribers for dispatch. Join-transaction subscribers always run on the calling
     * thread so they can participate in the surrounding Spring transaction.
     */
    protected void partitionSubscribers(Iterator<Subscriber> subscribers,
                                        boolean joinTransaction,
                                        List<Subscriber> syncSubscriberList,
                                        List<Subscriber> concurrentSubscriberList) {
        while (subscribers.hasNext()) {
            Subscriber subscriber = subscribers.next();
            if (subscriber.isJoinTransaction() != joinTransaction) {
                continue;
            }
            // joinTransaction must stay on the TX thread; concurrent execution cannot join TX.
            if (joinTransaction || subscriber instanceof SynchronizedSubscriber) {
                syncSubscriberList.add(subscriber);
            } else {
                concurrentSubscriberList.add(subscriber);
            }
        }
    }

    /**
     * Implementation of a {@link #perThreadDispatchQueue(ExpressionParser)} dispatcher.
     */
    private static final class PerThreadQueuedDispatcher extends Dispatcher {

        // This dispatcher matches the original dispatch behavior of EventBus.

        /**
         * Per-thread queue of events to dispatch.
         */
        private final ThreadLocal<Queue<EventAndSubscribers>> queue =
                ThreadLocal.withInitial(ArrayDeque::new);

        /**
         * Per-thread dispatch state, used to avoid reentrant event dispatching.
         */
        private final ThreadLocal<Boolean> dispatching = ThreadLocal.withInitial(() -> false);

        private PerThreadQueuedDispatcher(ExpressionParser expressionParser) {
            super(expressionParser);
        }

        @Override
        public DispatchInvokeResult dispatch(Object event, Iterator<Subscriber> subscribers, boolean joinTransaction) {

            checkNotNull(event);
            checkNotNull(subscribers);

            Queue<EventAndSubscribers> queueForThread = queue.get();
            queueForThread.offer(new EventAndSubscribers(event, subscribers));

            HandlerInterceptorContext context = new HandlerInterceptorContext();

            DispatchInvokeResult invokeResult = new DispatchInvokeResult(event);

            if (!dispatching.get()) {
                dispatching.set(true);
                try {
                    EventAndSubscribers nextEvent;
                    while ((nextEvent = queueForThread.poll()) != null) {

                        List<Subscriber> syncSubscriberList = new ArrayList<>();
                        List<Subscriber> concurrentSubscriberList = new ArrayList<>();
                        partitionSubscribers(nextEvent.getSubscribers(), joinTransaction,
                                syncSubscriberList, concurrentSubscriberList);

                        DispatchInvokeResult currentResult = new DispatchInvokeResult(nextEvent.getEvent());
                        currentResult = currentResult
                                .merge(dispatchConcurrentEvent(concurrentSubscriberList, nextEvent.getEvent(), context));
                        // sort sync subscriber
                        syncSubscriberList.sort((Comparator.comparingInt(Subscriber::getOrder)));
                        currentResult = currentResult
                                .merge(dispatchSyncEvent(syncSubscriberList, nextEvent.getEvent(), context));

                        // Nested posts of a different event must not be merged into the outer result.
                        if (Objects.equals(nextEvent.getEvent(), event)) {
                            invokeResult = invokeResult.merge(currentResult);
                        }
                    }
                } finally {
                    dispatching.remove();
                    queue.remove();
                }
            }
            return invokeResult;
        }

        @Getter
        private static final class EventAndSubscribers {

            private final Object event;
            private final Iterator<Subscriber> subscribers;

            private EventAndSubscribers(Object event, Iterator<Subscriber> subscribers) {
                this.event = event;
                this.subscribers = subscribers;
            }

        }
    }

    /**
     * Implementation of {@link #immediate(ExpressionParser)}.
     */
    private static final class ImmediateDispatcher extends Dispatcher {

        private ImmediateDispatcher(ExpressionParser expressionParser) {
            super(expressionParser);
        }

        @Override
        public DispatchInvokeResult dispatch(Object event, Iterator<Subscriber> subscribers, boolean joinTransaction) {
            checkNotNull(event);
            HandlerInterceptorContext context = new HandlerInterceptorContext();

            List<Subscriber> syncSubscriberList = new ArrayList<>();
            List<Subscriber> concurrentSubscriberList = new ArrayList<>();
            partitionSubscribers(subscribers, joinTransaction, syncSubscriberList, concurrentSubscriberList);

            // sort sync subscriber
            syncSubscriberList.sort((Comparator.comparingInt(Subscriber::getOrder)));

            return dispatchConcurrentEvent(concurrentSubscriberList, event, context)
                    .merge(dispatchSyncEvent(syncSubscriberList, event, context));
        }
    }

    /**
     * Implementation of {@link #asyncImmediate(ExpressionParser)}.
     */
    private static final class AsyncImmediateDispatcher extends Dispatcher {

        private AsyncImmediateDispatcher(ExpressionParser expressionParser) {
            super(expressionParser);
        }

        @Override
        public DispatchInvokeResult dispatch(Object event, Iterator<Subscriber> subscribers, boolean joinTransaction) {
            checkNotNull(event);
            HandlerInterceptorContext context = new HandlerInterceptorContext();

            List<Subscriber> syncSubscriberList = new ArrayList<>();
            List<Subscriber> concurrentSubscriberList = new ArrayList<>();
            partitionSubscribers(subscribers, joinTransaction, syncSubscriberList, concurrentSubscriberList);

            // sort sync subscriber
            syncSubscriberList.sort((Comparator.comparingInt(Subscriber::getOrder)));

            // Dispatch concurrent events without waiting
            DispatchInvokeResult concurrentResult = new DispatchInvokeResult(event);
            if (!CollectionUtils.isEmpty(concurrentSubscriberList)) {
                for (Subscriber subscriber : concurrentSubscriberList) {
                    if (!subscriber.shouldSubscribe(getExpressionParser(), event)) {
                        continue;
                    }
                    // Fire and forget
                    subscriber.concurrentDispatchEvent(event, context);
                }
            }

            return concurrentResult.merge(dispatchSyncEvent(syncSubscriberList, event, context));
        }
    }

    protected ExpressionParser getExpressionParser() {
        return expressionParser;
    }
}
