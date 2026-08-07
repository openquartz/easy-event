package com.openquartz.easyevent.core.intreceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.openquartz.easyevent.transfer.api.message.EventMessage;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class InterceptorListThreadSafetyTest {

    @Before
    public void setUp() throws Exception {
        resetPublisherInterceptors();
        resetTriggerInterceptors();
        resetHandlerInterceptors();
    }

    @After
    public void tearDown() throws Exception {
        resetPublisherInterceptors();
        resetTriggerInterceptors();
        resetHandlerInterceptors();
    }

    @Test
    public void publisherChain_shouldUseImmutableSnapshotList() throws Exception {
        String source = new String(Files.readAllBytes(
            Paths.get("src/main/java/com/openquartz/easyevent/core/intreceptor/PublisherInterceptorChain.java")),
            StandardCharsets.UTF_8);
        assertTrue(source.contains("volatile List<PublisherInterceptor>"));
        assertTrue(source.contains("Collections.unmodifiableList"));
        assertTrue(source.contains("synchronized void addInterceptor"));
    }

    @Test
    public void triggerChain_shouldUseImmutableSnapshotList() throws Exception {
        String source = new String(Files.readAllBytes(
            Paths.get("src/main/java/com/openquartz/easyevent/core/intreceptor/TriggerInterceptorChain.java")),
            StandardCharsets.UTF_8);
        assertTrue(source.contains("volatile List<TriggerInterceptor>"));
        assertTrue(source.contains("Collections.unmodifiableList"));
        assertTrue(source.contains("synchronized void addInterceptor"));
    }

    @Test
    public void handlerCenter_shouldUseCopyOnWriteArrayList() throws Exception {
        String source = new String(Files.readAllBytes(
            Paths.get("src/main/java/com/openquartz/easyevent/core/intreceptor/HandlerInterceptorCenter.java")),
            StandardCharsets.UTF_8);
        assertTrue(source.contains("CopyOnWriteArrayList"));
        assertTrue(source.contains("computeIfAbsent"));
    }

    @Test
    public void publisher_addOutOfOrder_shouldApplyLowerOrderFirst() {
        List<Integer> invocationOrder = Collections.synchronizedList(new ArrayList<>());
        PublisherInterceptorChain.addInterceptor(orderPublisherInterceptor(20, invocationOrder));
        PublisherInterceptorChain.addInterceptor(orderPublisherInterceptor(10, invocationOrder));

        PublisherInterceptorChain.applyPrePublish("evt", new PublisherInterceptorContext());

        assertEquals(2, invocationOrder.size());
        assertEquals(Integer.valueOf(10), invocationOrder.get(0));
        assertEquals(Integer.valueOf(20), invocationOrder.get(1));
    }

    @Test
    public void publisher_snapshotShouldBeUnmodifiable() throws Exception {
        PublisherInterceptorChain.addInterceptor(orderPublisherInterceptor(1, new ArrayList<>()));
        List<PublisherInterceptor> snapshot = getPublisherInterceptorList();
        boolean threw = false;
        try {
            snapshot.clear();
        } catch (UnsupportedOperationException ex) {
            threw = true;
        }
        assertTrue(threw);
    }

    @Test
    public void publisher_concurrentAddAndApply_shouldNotThrowCme() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(4);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger counter = new AtomicInteger();
        for (int i = 0; i < 1000; i++) {
            final int order = i;
            pool.submit(() -> {
                try {
                    start.await();
                    if (order % 2 == 0) {
                        PublisherInterceptorChain.addInterceptor(orderPublisherInterceptor(order, new ArrayList<>()));
                    } else {
                        PublisherInterceptorChain.applyPrePublish("evt", new PublisherInterceptorContext());
                    }
                    counter.incrementAndGet();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(1000, counter.get());
    }

    @Test
    public void trigger_addOutOfOrder_shouldApplyLowerOrderFirst() {
        List<Integer> invocationOrder = Collections.synchronizedList(new ArrayList<>());
        TriggerInterceptorChain.addInterceptor(orderTriggerInterceptor(20, invocationOrder));
        TriggerInterceptorChain.addInterceptor(orderTriggerInterceptor(10, invocationOrder));

        TriggerInterceptorChain.applyPreTrigger(new EventMessage(), new TriggerInterceptorContext());

        assertEquals(2, invocationOrder.size());
        assertEquals(Integer.valueOf(10), invocationOrder.get(0));
        assertEquals(Integer.valueOf(20), invocationOrder.get(1));
    }

    @Test
    public void trigger_concurrentAddAndApply_shouldNotThrowCme() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(4);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger counter = new AtomicInteger();
        for (int i = 0; i < 1000; i++) {
            final int order = i;
            pool.submit(() -> {
                try {
                    start.await();
                    if (order % 2 == 0) {
                        TriggerInterceptorChain.addInterceptor(orderTriggerInterceptor(order, new ArrayList<>()));
                    } else {
                        TriggerInterceptorChain.applyPreTrigger(new EventMessage(), new TriggerInterceptorContext());
                    }
                    counter.incrementAndGet();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(1000, counter.get());
    }

    @Test
    public void handlerCenter_addTwice_shouldMatchWithSortedOrder() {
        HandlerInterceptorCenter.addInterceptor(new StrInterceptor(5), StrInterceptor.class);
        HandlerInterceptorCenter.addInterceptor(new StrInterceptor(1), StrInterceptor.class);

        List<HandlerInterceptor> matched = HandlerInterceptorCenter.match(String.class);
        assertTrue(matched.size() >= 2);
        assertTrue(matched.get(0).order() <= matched.get(1).order());
    }

    @Test
    public void handlerCenter_concurrentAddSameType_shouldNotLoseEntries() throws Exception {
        int threads = 10;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            final int order = i;
            pool.submit(() -> {
                try {
                    start.await();
                    HandlerInterceptorCenter.addInterceptor(new StrInterceptor(order), StrInterceptor.class);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        assertTrue(done.await(5, TimeUnit.SECONDS));
        pool.shutdown();

        List<HandlerInterceptor> matched = HandlerInterceptorCenter.match(String.class);
        assertTrue(matched.size() >= threads);
    }

    private static PublisherInterceptor orderPublisherInterceptor(int order, List<Integer> invocationOrder) {
        return new PublisherInterceptor() {
            @Override
            public int order() {
                return order;
            }

            @Override
            public boolean prePublish(Object event, PublisherInterceptorContext context) {
                invocationOrder.add(order);
                return true;
            }
        };
    }

    private static TriggerInterceptor orderTriggerInterceptor(int order, List<Integer> invocationOrder) {
        return new TriggerInterceptor() {
            @Override
            public int order() {
                return order;
            }

            @Override
            public boolean preTrigger(EventMessage message, TriggerInterceptorContext context) {
                invocationOrder.add(order);
                return true;
            }
        };
    }

    static class StrInterceptor implements HandlerInterceptor<String> {
        private final int order;

        StrInterceptor(int order) {
            this.order = order;
        }

        @Override
        public int order() {
            return order;
        }
    }

    @SuppressWarnings("unchecked")
    private static List<PublisherInterceptor> getPublisherInterceptorList() throws Exception {
        Field field = PublisherInterceptorChain.class.getDeclaredField("INTERCEPTOR_LIST");
        field.setAccessible(true);
        return (List<PublisherInterceptor>) field.get(null);
    }

    private static void resetPublisherInterceptors() throws Exception {
        Field field = PublisherInterceptorChain.class.getDeclaredField("INTERCEPTOR_LIST");
        field.setAccessible(true);
        field.set(null, Collections.emptyList());
    }

    private static void resetTriggerInterceptors() throws Exception {
        Field field = TriggerInterceptorChain.class.getDeclaredField("INTERCEPTOR_LIST");
        field.setAccessible(true);
        field.set(null, Collections.emptyList());
    }

    @SuppressWarnings("unchecked")
    private static void resetHandlerInterceptors() throws Exception {
        Field field = HandlerInterceptorCenter.class.getDeclaredField("INTERCEPTOR_MAP");
        field.setAccessible(true);
        Map<Class<?>, List<HandlerInterceptor<?>>> map =
            (Map<Class<?>, List<HandlerInterceptor<?>>>) field.get(null);
        map.clear();
    }
}
