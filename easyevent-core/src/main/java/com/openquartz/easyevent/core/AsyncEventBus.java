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

import java.util.concurrent.ExecutorService;

import com.openquartz.easyevent.core.dispatcher.Dispatcher;
import com.openquartz.easyevent.core.expression.ExpressionParser;

/**
 * An {@link EventBus} that takes the Executor of your choice and uses it to dispatch events,
 * allowing dispatch to occur asynchronously.
 *
 * @author Cliff Biffle
 * @since 10.0
 */
public class AsyncEventBus extends EventBus {

    /**
     * Creates a new AsyncEventBus that will use {@code executor} to dispatch events. Assigns {@code
     * identifier} as the bus's name for logging purposes.
     *
     * @param identifier       short name for the bus, for logging purposes.
     * @param executor         Executor to use to dispatch events. It is the caller's responsibility to shut
     * @param expressionParser expression parser
     *                         down the executor after the last event has been posted to this event bus.
     */
    public AsyncEventBus(String identifier, ExecutorService executor, ExpressionParser expressionParser) {
        super(identifier, executor, Dispatcher.immediate(expressionParser), LoggingHandler.INSTANCE);
    }

    /**
     * Creates a new AsyncEventBus that will use {@code executor} to dispatch events.
     *
     * @param executor                   Executor to use to dispatch events. It is the caller's responsibility to shut
     *                                   down the executor after the last event has been posted to this event bus.
     * @param subscriberExceptionHandler Handler used to handle exceptions thrown from subscribers.
     *                                   See {@link SubscriberExceptionHandler} for more information.
     * @param expressionParser           expression parser
     * @since 16.0
     */
    public AsyncEventBus(ExecutorService executor, SubscriberExceptionHandler subscriberExceptionHandler, ExpressionParser expressionParser) {
        super("default", executor, Dispatcher.immediate(expressionParser), subscriberExceptionHandler);
    }

    /**
     * Creates a new AsyncEventBus that will use {@code executor} to dispatch events.
     *
     * @param executor         Executor to use to dispatch events. It is the caller's responsibility to shut
     *                         down the executor after the last event has been posted to this event bus.
     * @param expressionParser expression parser
     */
    public AsyncEventBus(ExecutorService executor, ExpressionParser expressionParser) {
        super("default", executor, Dispatcher.immediate(expressionParser), LoggingHandler.INSTANCE);
    }
}
