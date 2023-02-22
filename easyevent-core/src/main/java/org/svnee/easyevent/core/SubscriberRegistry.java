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

package org.svnee.easyevent.core;

import static org.svnee.easyevent.common.utils.ParamUtils.checkNotNull;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import org.svnee.easyevent.common.utils.Asserts;
import org.svnee.easyevent.common.utils.CollectionUtils;
import org.svnee.easyevent.common.utils.ExceptionUtils;
import org.svnee.easyevent.common.utils.reflect.TypeToken;
import org.svnee.easyevent.core.annotation.Subscribe;
import org.svnee.easyevent.core.exception.EventBusErrorCode;


/**
 * Registry of subscribers to a single event bus.
 *
 * @author Colin Decker
 */
final class SubscriberRegistry {

    /**
     * All registered subscribers, indexed by event type.
     *
     * <p>The {@link CopyOnWriteArraySet} values make it easy and relatively lightweight to get an
     * immutable snapshot of all current subscribers to an event without any locking.
     */
    private final ConcurrentMap<Class<?>, CopyOnWriteArraySet<Subscriber>> subscribers = new ConcurrentHashMap<>();

    /** The event bus this registry belongs to. */
    private final EventBus bus;

    SubscriberRegistry(EventBus bus) {

        checkNotNull(bus);

        this.bus = bus;
    }

    /**
     * Registers all subscriber methods on the given listener object.
     *
     * @param listener subscriber-event-handler
     */
    public void register(Object listener) {

        Map<Class<?>, List<Subscriber>> listenerMethods = findAllSubscribers(listener);

        for (Entry<Class<?>, List<Subscriber>> entry : listenerMethods.entrySet()) {

            Class<?> eventType = entry.getKey();
            Collection<Subscriber> eventMethodsInListener = entry.getValue();
            CopyOnWriteArraySet<Subscriber> eventSubscribers = subscribers.get(eventType);

            if (eventSubscribers == null) {

                CopyOnWriteArraySet<Subscriber> newSet = new CopyOnWriteArraySet<>();

                CopyOnWriteArraySet<Subscriber> subscriberList = this.subscribers.putIfAbsent(eventType, newSet);
                if (subscriberList != null) {
                    eventSubscribers = subscriberList;
                } else {
                    eventSubscribers = newSet;
                }
            }

            eventSubscribers.addAll(eventMethodsInListener);
        }
    }

    /**
     * Unregisters all subscribers on the given listener object.
     *
     * @param listener subscriber-event-handler
     */
    void unregister(Object listener) {

        // eventType --> subscriber-event-handler
        Map<Class<?>, List<Subscriber>> listenerMethods = findAllSubscribers(listener);

        for (Entry<Class<?>, List<Subscriber>> entry : listenerMethods.entrySet()) {

            Class<?> eventType = entry.getKey();
            Collection<Subscriber> listenerMethodsForType = entry.getValue();

            CopyOnWriteArraySet<Subscriber> currentSubscribers = subscribers.get(eventType);
            if (currentSubscribers == null || !currentSubscribers.removeAll(listenerMethodsForType)) {
                // if removeAll returns true, all we really know is that at least one subscriber was
                // removed... however, barring something very strange we can assume that if at least one
                // subscriber was removed, all subscribers on listener for that event type were... after
                // all, the definition of subscribers on a particular class is totally static
                throw new IllegalArgumentException(
                    "missing event subscriber for an annotated method. Is " + listener + " registered?");
            }

            // don't try to remove the set if it's empty; that can't be done safely without a lock
            // anyway, if the set is empty it'll just be wrapping an array of length 0
        }
    }

    public Set<Subscriber> getSubscribers(Class<?> eventType) {
        CopyOnWriteArraySet<Subscriber> subscriberSet = this.subscribers.get(eventType);
        return Objects.nonNull(subscriberSet) ? subscriberSet : Collections.emptySet();
    }

    /**
     * Gets an iterator representing an immutable snapshot of all subscribers to the given event at
     * the time this method is called.
     */
    public Iterator<Subscriber> getSubscribers(Object event) {

        Set<Class<?>> eventTypes = flattenHierarchy(event.getClass());

        List<Iterator<Subscriber>> subscriberIterators = new ArrayList<>(eventTypes.size());

        for (Class<?> eventType : eventTypes) {
            CopyOnWriteArraySet<Subscriber> eventSubscribers = subscribers.get(eventType);
            if (eventSubscribers != null) {
                // eager no-copy snapshot
                subscriberIterators.add(eventSubscribers.iterator());
            }
        }

        return CollectionUtils.concat(subscriberIterators.iterator());
    }

    /**
     * A thread-safe cache that contains the mapping from each class to all methods in that class and
     * all super-classes, that are annotated with {@code @Subscribe}. The cache is shared across all
     * instances of this class; this greatly improves performance if multiple EventBus instances are
     * created and objects of the same class are registered on all of them.
     */
    private static final Cache<Class<?>, List<Method>> SUBSCRIBER_METHODS_CACHE =
        Caffeine.newBuilder()
            .weakKeys()
            .build();

    /**
     * Returns all subscribers for the given listener grouped by the type of event they subscribe to.
     */
    private Map<Class<?>, List<Subscriber>> findAllSubscribers(Object listener) {

        Map<Class<?>, List<Subscriber>> methodsInListener = new HashMap<>();
        Class<?> clazz = listener.getClass();

        for (Method method : getAnnotatedMethods(clazz)) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            Class<?> eventType = parameterTypes[0];
            List<Subscriber> subscriberList = methodsInListener.getOrDefault(eventType, new LinkedList<>());
            subscriberList.add(Subscriber.create(bus, listener, method));
            methodsInListener.put(eventType, subscriberList);
        }
        return methodsInListener;
    }

    private static List<Method> getAnnotatedMethods(Class<?> clazz) {
        try {
            return SUBSCRIBER_METHODS_CACHE.get(clazz, SubscriberRegistry::getAnnotatedMethodsNotCached);
        } catch (Exception e) {
            ExceptionUtils.throwIfUnchecked(e.getCause());
            throw e;
        }
    }

    private static List<Method> getAnnotatedMethodsNotCached(Class<?> clazz) {
        Set<? extends Class<?>> supertypes = TypeToken.of(clazz).getTypes().rawTypes();
        Map<MethodIdentifier, Method> identifiers = new HashMap<>();
        for (Class<?> supertype : supertypes) {
            for (Method method : supertype.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Subscribe.class) && !method.isSynthetic()) {

                    Class<?>[] parameterTypes = method.getParameterTypes();

                    Asserts.isTrue(parameterTypes.length == 1,
                        EventBusErrorCode.SUBSCRIBER_METHOD_ARGS_NUM_ERROR,
                        method,
                        parameterTypes.length);

                    Asserts.isTrue(!parameterTypes[0].isPrimitive(),
                        EventBusErrorCode.SUBSCRIBER_METHOD_ARGS_POSITIVE_ERROR,
                        method,
                        parameterTypes[0].getName());

                    MethodIdentifier ident = new MethodIdentifier(method);
                    identifiers.putIfAbsent(ident, method);
                }
            }
        }
        return CollectionUtils.newArrayList(identifiers.values());
    }

    /** Global cache of classes to their flattened hierarchy of supertypes. */
    private static final Cache<Class<?>, Set<Class<?>>> FLATTEN_HIERARCHY_CACHE =
        Caffeine.newBuilder()
            .weakKeys()
            .build();

    /**
     * Flattens a class's type hierarchy into a set of {@code Class} objects including all
     * superclasses (transitively) and all interfaces implemented by these superclasses.
     */
    static Set<Class<?>> flattenHierarchy(Class<?> concreteClass) {
        try {
            return FLATTEN_HIERARCHY_CACHE
                .get(concreteClass, key -> (Set<Class<?>>) TypeToken.of(concreteClass).getTypes().rawTypes());
        } catch (Exception e) {
            ExceptionUtils.throwIfUnchecked(e);
            throw e;
        }
    }

    private static final class MethodIdentifier {

        private final String name;
        private final List<Class<?>> parameterTypes;

        MethodIdentifier(Method method) {
            this.name = method.getName();
            this.parameterTypes = Arrays.asList(method.getParameterTypes());
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(new Object[]{name, parameterTypes});
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof MethodIdentifier) {
                MethodIdentifier ident = (MethodIdentifier) o;
                return name.equals(ident.name) && parameterTypes.equals(ident.parameterTypes);
            }
            return false;
        }
    }
}
