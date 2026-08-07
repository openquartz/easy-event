package com.openquartz.easyevent.core.dispatcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.openquartz.easyevent.core.EventBus;
import com.openquartz.easyevent.core.Subscriber;
import com.openquartz.easyevent.core.annotation.AllowConcurrentEvents;
import com.openquartz.easyevent.core.annotation.Subscribe;
import com.openquartz.easyevent.core.expression.ExpressionParser;
import com.openquartz.easyevent.core.intreceptor.HandlerInterceptor;
import com.openquartz.easyevent.core.intreceptor.HandlerInterceptorCenter;
import com.openquartz.easyevent.core.intreceptor.HandlerInterceptorContext;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DispatcherBugFixTest {

    private ExpressionParser alwaysTrueParser;
    private Dispatcher dispatcher;
    private EventBus eventBus;

    @Before
    public void setUp() throws Exception {
        alwaysTrueParser = (expression, event, targetMethod, targetClass) -> true;
        dispatcher = Dispatcher.perThreadDispatchQueue(alwaysTrueParser);
        eventBus = new EventBus("test", (exception, context) -> {
            if (exception instanceof Error) {
                throw (Error) exception;
            }
            if (exception instanceof RuntimeException) {
                throw (RuntimeException) exception;
            }
            throw new RuntimeException(exception);
        }, alwaysTrueParser);
        clearHandlerInterceptors();
    }

    @After
    public void tearDown() throws Exception {
        clearHandlerInterceptors();
    }

    @SuppressWarnings("unchecked")
    private static void clearHandlerInterceptors() throws Exception {
        Field field = HandlerInterceptorCenter.class.getDeclaredField("INTERCEPTOR_MAP");
        field.setAccessible(true);
        ((Map<?, ?>) field.get(null)).clear();
    }

    private Subscriber subscriber(Object listener, String methodName) throws Exception {
        Method method = listener.getClass().getMethod(methodName, Object.class);
        Method create = Subscriber.class.getDeclaredMethod("create", EventBus.class, Object.class, Method.class);
        create.setAccessible(true);
        return (Subscriber) create.invoke(null, eventBus, listener, method);
    }

    @Test
    public void nestedDifferentEvent_shouldNotThrowOnMerge() throws Exception {
        Object eventA = new Object();
        Object eventB = new Object();

        class NestedListener {
            @Subscribe(joinTransaction = true)
            public void onB(Object event) {
            }
        }
        Subscriber nested = subscriber(new NestedListener(), "onB");

        class OuterListener {
            @Subscribe(joinTransaction = true)
            public void onA(Object event) {
                dispatcher.dispatch(eventB, Collections.singletonList(nested).iterator(), true);
            }
        }
        Subscriber outer = subscriber(new OuterListener(), "onA");

        DispatchInvokeResult result = dispatcher.dispatch(eventA, Collections.singletonList(outer).iterator(), true);

        assertTrue(result.isSuccess());
        assertEquals(1, result.getSuccessSubscriberList().size());
    }

    @Test
    public void interceptorVeto_shouldNotMarkSubscriberSuccess() throws Exception {
        HandlerInterceptorCenter.addInterceptor(new VetoInterceptor(), VetoInterceptor.class);

        class Listener {
            @Subscribe(joinTransaction = true)
            public void on(Object event) {
            }
        }
        Subscriber subscriber = subscriber(new Listener(), "on");
        DispatchInvokeResult result =
            dispatcher.dispatch(new Object(), Collections.singletonList(subscriber).iterator(), true);

        assertTrue(result.isSuccess());
        assertTrue(result.getSuccessSubscriberList().isEmpty());
    }

    @Test
    public void syncFailure_shouldKeepAlreadySuccessfulSubscribers() throws Exception {
        List<String> invoked = new CopyOnWriteArrayList<>();

        class First {
            @Subscribe(joinTransaction = true)
            public void on(Object event) {
                invoked.add("first");
            }
        }
        class Second {
            @Subscribe(joinTransaction = true)
            public void on(Object event) {
                throw new RuntimeException("fail");
            }
        }

        Subscriber first = subscriber(new First(), "on");
        Subscriber second = subscriber(new Second(), "on");

        DispatchInvokeResult result =
            dispatcher.dispatch(new Object(), Arrays.asList(first, second).iterator(), true);

        assertFalse(result.isSuccess());
        assertEquals(Collections.singletonList("first"), invoked);
        assertEquals(1, result.getSuccessSubscriberList().size());
        assertTrue(result.getSuccessSubscriberList().get(0).getTargetIdentify().contains("First"));
    }

    @Test
    public void joinTransaction_shouldForceConcurrentSubscriberOntoSyncPath() throws Exception {
        AtomicBoolean ranOnCallerThread = new AtomicBoolean(false);
        Thread caller = Thread.currentThread();

        class ConcurrentJoinListener {
            @AllowConcurrentEvents
            @Subscribe(joinTransaction = true)
            public void on(Object event) {
                ranOnCallerThread.set(Thread.currentThread() == caller);
            }
        }

        Subscriber subscriber = subscriber(new ConcurrentJoinListener(), "on");
        assertFalse(subscriber instanceof Subscriber.SynchronizedSubscriber);

        DispatchInvokeResult result =
            dispatcher.dispatch(new Object(), Collections.singletonList(subscriber).iterator(), true);

        assertTrue(result.isSuccess());
        assertTrue(ranOnCallerThread.get());
        assertEquals(1, result.getSuccessSubscriberList().size());
    }

    @Test
    public void immediateDispatcher_usesOwnExpressionParserInstance() throws Exception {
        ExpressionParser parserA = (expression, event, targetMethod, targetClass) -> true;
        ExpressionParser parserB = (expression, event, targetMethod, targetClass) -> false;

        Dispatcher dispatcherA = Dispatcher.immediate(parserA);
        Dispatcher dispatcherB = Dispatcher.immediate(parserB);

        class Listener {
            @Subscribe(joinTransaction = true, condition = "true")
            public void on(Object event) {
            }
        }
        Subscriber subscriber = subscriber(new Listener(), "on");

        DispatchInvokeResult resultA =
            dispatcherA.dispatch(new Object(), Collections.singletonList(subscriber).iterator(), true);
        DispatchInvokeResult resultB =
            dispatcherB.dispatch(new Object(), Collections.singletonList(subscriber).iterator(), true);

        assertEquals(1, resultA.getSuccessSubscriberList().size());
        assertTrue(resultB.getSuccessSubscriberList().isEmpty());
    }

    private static final class VetoInterceptor implements HandlerInterceptor<Object> {
        @Override
        public boolean preHandle(Object event, Object handler, HandlerInterceptorContext context) {
            return false;
        }
    }
}
