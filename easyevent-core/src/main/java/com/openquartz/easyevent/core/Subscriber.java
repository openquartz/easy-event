package com.openquartz.easyevent.core;

import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotNull;

import com.openquartz.easyevent.common.utils.StringUtils;
import com.openquartz.easyevent.core.annotation.Subscribe;
import com.openquartz.easyevent.core.expression.ExpressionParser;
import com.openquartz.easyevent.core.intreceptor.HandlerInterceptorContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.openquartz.easyevent.core.annotation.AllowConcurrentEvents;
import com.openquartz.easyevent.core.annotation.Order;
import com.openquartz.easyevent.core.intreceptor.HandlerInterceptorChain;

/**
 * 服务订阅者
 *
 * @author svnee
 */
public class Subscriber {

    /**
     * Creates a {@code Subscriber} for {@code method} on {@code listener}.
     */
    static Subscriber create(EventBus bus, Object listener, Method method) {
        return isDeclaredThreadSafe(method)
                ? new Subscriber(bus, listener, method)
                : new SynchronizedSubscriber(bus, listener, method);
    }

    /**
     * The event bus this subscriber belongs to.
     */
    private final EventBus bus;

    /**
     * The object with the subscriber method.
     */
    final Object target;

    /**
     * Subscriber method.
     */
    private final Method method;

    /**
     * subscriber order
     */
    private final int order;

    /**
     * Executor to use for dispatching events to this subscriber.
     */
    private final ExecutorService executor;

    /**
     * join this event transaction
     */
    private final boolean joinTransaction;

    private Subscriber(EventBus bus, Object target, Method method) {

        checkNotNull(target);

        this.bus = bus;
        this.target = target;
        this.method = method;
        method.setAccessible(true);

        this.executor = bus.executor();
        Order orderAnnotation = method.getAnnotation(Order.class);
        this.order = Objects.nonNull(orderAnnotation) ? orderAnnotation.value() : Integer.MAX_VALUE;

        Subscribe subscribeAnnotation = method.getDeclaredAnnotation(Subscribe.class);
        this.joinTransaction = Objects.nonNull(subscribeAnnotation) && subscribeAnnotation.joinTransaction();
    }

    /**
     * Dispatches {@code event} to this subscriber using the proper executor.
     */
    public final void dispatchEvent(Object event, HandlerInterceptorContext context) {
        executor.execute(() -> doDispatchEvent(event, context));
    }

    public final void singleDispatchEvent(Object event, HandlerInterceptorContext context) {
        doDispatchEvent(event, context);
    }

    public final Future<Boolean> concurrentDispatchEvent(Object event, HandlerInterceptorContext context) {
        return executor.submit(() -> doDispatchEvent(event, context));
    }

    /**
     * do dispatcher trigger event!
     *
     * @param event   event
     * @param context context
     * @return invoke flag
     */
    private boolean doDispatchEvent(Object event, HandlerInterceptorContext context) {
        HandlerInterceptorChain chain = new HandlerInterceptorChain(target, event.getClass());
        boolean handle = chain.applyPreHandle(event, context);
        if (!handle) {
            return false;
        }

        try {
            doInvokeSubscriberMethod(event);
        } catch (Exception ex) {
            chain.triggerAfterCompletion(event, context, 0, ex);
            throw ex;
        }
        chain.triggerAfterCompletion(event, context, 0, null);
        return true;
    }

    /**
     *
     */
    private void doInvokeSubscriberMethod(Object event) {
        try {
            invokeSubscriberMethod(event);
        } catch (InvocationTargetException e) {
            bus.handleSubscriberException(e.getCause(), context(event));
        }
    }

    /**
     * Invokes the subscriber method. This method can be overridden to make the invocation
     * synchronized.
     */
    void invokeSubscriberMethod(Object event) throws InvocationTargetException {
        try {
            checkNotNull(event);
            method.invoke(target, event);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Method rejected target/argument: " + event, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Method became inaccessible: " + event, e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw e;
        }
    }

    /**
     * Gets the context for the given event.
     */
    private SubscriberExceptionContext context(Object event) {
        return new SubscriberExceptionContext(bus, event, target, method);
    }

    @Override
    public final int hashCode() {
        return (31 + method.hashCode()) * 31 + System.identityHashCode(target);
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj instanceof Subscriber) {
            Subscriber that = (Subscriber) obj;
            // Use == so that different equal instances will still receive events.
            // We only guard against the case that the same object is registered
            // multiple times
            return target == that.target && method.equals(that.method);
        }
        return false;
    }

    /**
     * Checks whether {@code method} is thread-safe, as indicated by the presence of the {@link
     * AllowConcurrentEvents} annotation.
     */
    private static boolean isDeclaredThreadSafe(Method method) {
        return method.getAnnotation(AllowConcurrentEvents.class) != null;
    }

    /**
     * 获取唯一表示符号
     *
     * @return 标识符号
     */
    public String getTargetIdentify() {
        return target.getClass().getName() + "#" + method.getName();
    }

    /**
     * order index
     */
    public int getOrder() {
        return order;
    }

    /**
     * is join the transaction
     *
     * @return is join the transaction
     */
    public boolean isJoinTransaction() {
        return joinTransaction;
    }

    /**
     * should subscribe expression
     */
    public boolean shouldSubscribe(ExpressionParser expressionParser, Object event) {

        if (Objects.isNull(expressionParser)) {
            return true;
        }

        Class<?> targetClass = this.target.getClass();
        Subscribe annotation = this.method.getDeclaredAnnotation(Subscribe.class);
        if (annotation == null || StringUtils.isBlank(annotation.condition())) {
            return true;
        }
        return expressionParser.parse(annotation.condition(), event, method, targetClass);
    }

    /**
     * Subscriber that synchronizes invocations of a method to ensure that only one thread may enter
     * the method at a time.
     */
    public static final class SynchronizedSubscriber extends Subscriber {

        private SynchronizedSubscriber(EventBus bus, Object target, Method method) {
            super(bus, target, method);
        }

        @Override
        void invokeSubscriberMethod(Object event) throws InvocationTargetException {
            synchronized (this) {
                super.invokeSubscriberMethod(event);
            }
        }
    }
}
