/*
 * Copyright (C) 2014 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.svnee.easyevent.core.dispatcher;

import static org.svnee.easyevent.common.utils.ParamUtils.checkNotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.svnee.easyevent.core.Subscriber;
import org.svnee.easyevent.core.Subscriber.SynchronizedSubscriber;
import org.svnee.easyevent.core.intreceptor.HandlerInterceptorContext;
import org.svnee.easyevent.common.model.Pair;
import org.svnee.easyevent.common.utils.CollectionUtils;

/**
 * Dispatcher
 *
 * @author svnee
 */
@Slf4j
public abstract class Dispatcher {

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
    public static Dispatcher perThreadDispatchQueue() {
        return new PerThreadQueuedDispatcher();
    }

    /**
     * Returns a dispatcher that dispatches events to subscribers immediately as they're posted
     * without using an intermediate queue to change the dispatch order. This is effectively a
     * depth-first dispatch order, vs. breadth-first when using a queue.
     */
    public static Dispatcher immediate() {
        return ImmediateDispatcher.INSTANCE;
    }

    /** Dispatches the given {@code event} to the given {@code subscribers}. */
    public abstract DispatchInvokeResult dispatch(Object event, Iterator<Subscriber> subscribers);

    protected DispatchInvokeResult dispatchConcurrentEvent(List<Subscriber> concurrentSubscriberList,
        Object event,
        HandlerInterceptorContext context) {

        DispatchInvokeResult invokeResult = new DispatchInvokeResult(event);

        if (CollectionUtils.isEmpty(concurrentSubscriberList)) {
            return invokeResult;
        }

        List<Pair<Future<Boolean>, Subscriber>> dispatchFutureList = new ArrayList<>();
        for (Subscriber subscriber : concurrentSubscriberList) {
            Future<Boolean> dispatchFuture = subscriber.concurrentDispatchEvent(event, context);
            dispatchFutureList.add(Pair.of(dispatchFuture, subscriber));
        }
        for (Pair<Future<Boolean>, Subscriber> pair : dispatchFutureList) {
            try {
                pair.getLeft().get();
                invokeResult.addSubscriber(pair.getRight());
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
                subscriber.singleDispatchEvent(event, context);
                invokeResult.addSubscriber(subscriber);
            } catch (Exception ex) {
                invokeResult.setInvokeError(ex);
                log.error("[Dispatcher#dispatchSyncEvent] event:{},subscriber:{} invoke-error!", event,
                    subscriber.getTargetIdentify(), ex);
                invokeResult.clear();
                return invokeResult;
            }
        }
        return invokeResult;
    }

    /** Implementation of a {@link #perThreadDispatchQueue()} dispatcher. */
    private static final class PerThreadQueuedDispatcher extends Dispatcher {

        // This dispatcher matches the original dispatch behavior of EventBus.

        /** Per-thread queue of events to dispatch. */
        private final ThreadLocal<Queue<EventAndSubscribers>> queue =
            ThreadLocal.withInitial(ArrayDeque::new);

        /** Per-thread dispatch state, used to avoid reentrant event dispatching. */
        private final ThreadLocal<Boolean> dispatching = ThreadLocal.withInitial(() -> false);

        @Override
        public DispatchInvokeResult dispatch(Object event, Iterator<Subscriber> subscribers) {

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
                        while (nextEvent.getSubscribers().hasNext()) {
                            Subscriber subscriber = nextEvent.getSubscribers().next();
                            if (subscriber instanceof SynchronizedSubscriber) {
                                syncSubscriberList.add(subscriber);
                            } else {
                                concurrentSubscriberList.add(subscriber);
                            }
                        }

                        invokeResult = invokeResult
                            .merge(dispatchConcurrentEvent(concurrentSubscriberList, nextEvent.getEvent(), context));
                        invokeResult = invokeResult
                            .merge(dispatchSyncEvent(syncSubscriberList, nextEvent.getEvent(), context));
                    }
                } finally {
                    dispatching.remove();
                    queue.remove();
                }
            }
            return invokeResult;
        }

        private static final class EventAndSubscribers {

            private final Object event;
            private final Iterator<Subscriber> subscribers;

            private EventAndSubscribers(Object event, Iterator<Subscriber> subscribers) {
                this.event = event;
                this.subscribers = subscribers;
            }

            public Object getEvent() {
                return event;
            }

            public Iterator<Subscriber> getSubscribers() {
                return subscribers;
            }
        }
    }

    /** Implementation of {@link #immediate()}. */
    private static final class ImmediateDispatcher extends Dispatcher {

        private static final ImmediateDispatcher INSTANCE = new ImmediateDispatcher();

        @Override
        public DispatchInvokeResult dispatch(Object event, Iterator<Subscriber> subscribers) {
            checkNotNull(event);
            HandlerInterceptorContext context = new HandlerInterceptorContext();

            List<Subscriber> syncSubscriberList = new ArrayList<>();
            List<Subscriber> concurrentSubscriberList = new ArrayList<>();

            while (subscribers.hasNext()) {
                Subscriber subscriber = subscribers.next();
                if (subscriber instanceof SynchronizedSubscriber) {
                    syncSubscriberList.add(subscriber);
                } else {
                    concurrentSubscriberList.add(subscriber);
                }
            }

            return dispatchConcurrentEvent(concurrentSubscriberList, event, context)
                .merge(dispatchSyncEvent(syncSubscriberList, event, context));
        }
    }
}
