/*
 * Copyright (C) 2007 The Guava Authors
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

package com.openquartz.easyevent.core;


import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.openquartz.easyevent.common.concurrent.DirectExecutor;
import com.openquartz.easyevent.common.utils.CollectionUtils;
import com.openquartz.easyevent.core.dispatcher.DispatchInvokeResult;
import com.openquartz.easyevent.core.dispatcher.Dispatcher;
import com.openquartz.easyevent.core.expression.ExpressionParser;

/**
 * EventBus
 *
 * @author svnee
 */
public class EventBus {

    private final String identifier;
    private final ExecutorService executor;
    private final SubscriberExceptionHandler exceptionHandler;
    private final SubscriberRegistry subscribers = new SubscriberRegistry(this);
    private final Dispatcher dispatcher;

    /**
     * Creates a new EventBus named "default".
     */
    public EventBus() {
        this("default");
    }

    /**
     * Creates a new EventBus with the given {@code identifier}.
     *
     * @param identifier a brief name for this bus, for logging purposes. Should be a valid Java
     *                   identifier.
     */
    public EventBus(String identifier) {
        this(
                identifier,
                DirectExecutor.INSTANCE,
                Dispatcher.perThreadDispatchQueue(null),
                LoggingHandler.INSTANCE);
    }

    public EventBus(String identifier, SubscriberExceptionHandler subscriberExceptionHandler, ExpressionParser expressionParser) {
        this(
                identifier,
                DirectExecutor.INSTANCE,
                Dispatcher.perThreadDispatchQueue(expressionParser),
                subscriberExceptionHandler);
    }

    /**
     * Creates a new EventBus with the given {@link SubscriberExceptionHandler}.
     *
     * @param exceptionHandler Handler for subscriber exceptions.
     * @since 16.0
     */
    public EventBus(SubscriberExceptionHandler exceptionHandler, ExecutorService executor, ExpressionParser expressionParser) {
        this(
                "default",
                executor,
                Dispatcher.perThreadDispatchQueue(expressionParser),
                exceptionHandler);
    }

    public EventBus(
            String identifier,
            ExecutorService executor,
            Dispatcher dispatcher,
            SubscriberExceptionHandler exceptionHandler) {

        checkNotNull(identifier);
        checkNotNull(executor);
        checkNotNull(dispatcher);
        checkNotNull(exceptionHandler);

        this.identifier = identifier;
        this.executor = executor;
        this.dispatcher = dispatcher;
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Returns the identifier for this event bus.
     *
     * @since 19.0
     */
    public final String identifier() {
        return identifier;
    }

    /**
     * Returns the default executor this event bus uses for dispatching events to subscribers.
     */
    final ExecutorService executor() {
        return executor;
    }

    /**
     * Handles the given exception thrown by a subscriber with the given context.
     */
    void handleSubscriberException(Throwable e, SubscriberExceptionContext context) {
        checkNotNull(e);
        checkNotNull(context);
        exceptionHandler.handleException(e, context);
    }

    /**
     * Registers all subscriber methods on {@code object} to receive events.
     *
     * @param object object whose subscriber methods should be registered.
     */
    public void register(Object object) {
        subscribers.register(object);
    }

    /**
     * Unregisters all subscriber methods on a registered {@code object}.
     *
     * @param object object whose subscriber methods should be unregistered.
     * @throws IllegalArgumentException if the object was not previously registered.
     */
    public void unregister(Object object) {
        subscribers.unregister(object);
    }

    /**
     * Posts an event to all registered subscribers. This method will return successfully after the
     * event has been posted to all subscribers, and regardless of any exceptions thrown by
     * subscribers.
     *
     * <p>If no subscribers have been subscribed for {@code event}'s class, and {@code event} is not
     * already a {@link DeadEvent}, it will be wrapped in a DeadEvent and reposted.
     *
     * @param event event to post.
     */
    public DispatchInvokeResult post(Object event, boolean joinTransaction) {
        Iterator<Subscriber> eventSubscribers = this.getSubscribers(event);
        if (eventSubscribers.hasNext()) {
            return dispatcher.dispatch(event, eventSubscribers, joinTransaction);
        } else if (!(event instanceof DeadEvent)) {
            // the event had no subscribers and was not itself a DeadEvent
            return post(new DeadEvent(this, event), joinTransaction);
        }
        return new DispatchInvokeResult(event);
    }

    public DispatchInvokeResult post(Object event) {
        DispatchInvokeResult noJoinTransactionInvokeResult = post(event, false);
        DispatchInvokeResult joinTransactionInvokeResult = post(event, true);
        return noJoinTransactionInvokeResult.merge(joinTransactionInvokeResult);
    }

    public DispatchInvokeResult postAll(Object event, List<String> excludeIdentifySubscribers, boolean joinTransaction) {
        if (CollectionUtils.isEmpty(excludeIdentifySubscribers)) {
            return post(event, joinTransaction);
        }
        return post(event, excludeIdentifySubscribers, joinTransaction);
    }

    /**
     * 指定排除触发
     *
     * @param event                      事件
     * @param excludeIdentifySubscribers 排除指定订阅者
     * @return 执行结果
     */
    public DispatchInvokeResult post(Object event, List<String> excludeIdentifySubscribers, boolean joinTransaction) {
        Iterator<Subscriber> eventSubscribers = this.getSubscribers(event);
        if (eventSubscribers.hasNext()) {
            List<Subscriber> specSubscriberList = new ArrayList<>();
            eventSubscribers.forEachRemaining(k -> {
                if (!excludeIdentifySubscribers.contains(k.getTargetIdentify())) {
                    specSubscriberList.add(k);
                }
            });
            if (CollectionUtils.isNotEmpty(specSubscriberList)) {
                return dispatcher.dispatch(event, specSubscriberList.iterator(), joinTransaction);
            }
        } else if (!(event instanceof DeadEvent)) {
            // the event had no subscribers and was not itself a DeadEvent
            return post(new DeadEvent(this, event), joinTransaction);
        }
        return new DispatchInvokeResult(event);
    }

    /**
     * 获取订阅者
     *
     * @param event event
     * @return Iterator<Subscriber>
     */
    public Iterator<Subscriber> getSubscribers(Object event) {
        return this.subscribers.getSubscribers(event);
    }

    @Override
    public String toString() {
        return "EventBus{" +
                "identifier='" + identifier + '\'' +
                '}';
    }

    /**
     * Simple logging handler for subscriber exceptions.
     */
    static final class LoggingHandler implements SubscriberExceptionHandler {

        static final LoggingHandler INSTANCE = new LoggingHandler();

        @Override
        public void handleException(Throwable exception, SubscriberExceptionContext context) {
            Logger logger = logger(context);
            if (logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, message(context), exception);
            }
        }

        private static Logger logger(SubscriberExceptionContext context) {
            return Logger.getLogger(EventBus.class.getName() + "." + context.getEventBus().identifier());
        }

        private static String message(SubscriberExceptionContext context) {
            Method method = context.getSubscriberMethod();
            return "Exception thrown by subscriber method "
                    + method.getName()
                    + '('
                    + method.getParameterTypes()[0].getName()
                    + ')'
                    + " on subscriber "
                    + context.getSubscriber()
                    + " when dispatching event: "
                    + context.getEvent();
        }
    }
}
