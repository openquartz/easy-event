/*
 * Copyright (C) 2013 The Guava Authors
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

/**
 * Context for an exception thrown by a subscriber.
 *
 * 源代码链接自：Google Guava (com.google.common.eventbus.SubscriberExceptionContext)
 * <link>https://github.com/google/guava/blob/master/guava/src/com/google/common/eventbus/SubscriberExceptionContext.java</link>
 *
 * @author svnee
 * @since 16.0
 */
public class SubscriberExceptionContext {

  private final EventBus eventBus;
  private final Object event;
  private final Object subscriber;
  private final Method subscriberMethod;

  /**
   * @param eventBus The {@link EventBus} that handled the event and the subscriber. Useful for
   *     broadcasting a new event based on the error.
   * @param event The event object that caused the subscriber to throw.
   * @param subscriber The source subscriber context.
   * @param subscriberMethod the subscribed method.
   */
  SubscriberExceptionContext(
      EventBus eventBus, Object event, Object subscriber, Method subscriberMethod) {

      checkNotNull(eventBus);
      checkNotNull(event);
      checkNotNull(subscriber);
      checkNotNull(subscriberMethod);

      this.eventBus = eventBus;
      this.event = event;
      this.subscriber = subscriber;
      this.subscriberMethod = subscriberMethod;
  }

  /**
   * @return The {@link EventBus} that handled the event and the subscriber. Useful for broadcasting
   *     a new event based on the error.
   */
  public EventBus getEventBus() {
    return eventBus;
  }

  /** @return The event object that caused the subscriber to throw. */
  public Object getEvent() {
    return event;
  }

  /** @return The object context that the subscriber was called on. */
  public Object getSubscriber() {
    return subscriber;
  }

  /** @return The subscribed method that threw the exception. */
  public Method getSubscriberMethod() {
    return subscriberMethod;
  }
}
