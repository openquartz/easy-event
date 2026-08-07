package com.openquartz.easyevent.common.concurrent.lock.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyevent.common.concurrent.lock.LockBizType;
import com.openquartz.easyevent.common.exception.EasyEventException;
import com.openquartz.easyevent.common.model.Pair;
import com.openquartz.easyevent.common.concurrent.lock.DistributedLockFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import org.junit.Test;

public class LockSupportImplTest {

    @Test
    public void shouldNotUseExpiringCaffeineLocalLocks() throws Exception {
        String source = new String(Files.readAllBytes(
            Paths.get("src/main/java/com/openquartz/easyevent/common/concurrent/lock/impl/LockSupportImpl.java")),
            StandardCharsets.UTF_8);
        assertFalse(source.contains("Caffeine"));
        assertFalse(source.contains("expireAfterAccess"));
        assertTrue(source.contains("ConcurrentHashMap"));
        assertTrue(source.contains("computeIfAbsent"));
    }

    @Test
    public void tryLock_sameKey_shouldSerializeConsumers() throws Exception {
        LockSupportImpl lockSupport = new LockSupportImpl(null);
        Pair<String, LockBizType> key = Pair.of("event-1", LockBizType.EVENT_HANDLE);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch hold = new CountDownLatch(1);
        AtomicInteger entered = new AtomicInteger();
        AtomicBoolean secondGotLock = new AtomicBoolean(true);

        Thread holder = new Thread(() -> lockSupport.consumeIfTryLock(key, () -> {
            entered.incrementAndGet();
            started.countDown();
            try {
                hold.await(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
        holder.start();
        assertTrue(started.await(3, TimeUnit.SECONDS));

        Thread contender = new Thread(() -> {
            boolean ok = lockSupport.consumeIfTryLock(key, () -> entered.incrementAndGet());
            secondGotLock.set(ok);
        });
        contender.start();
        contender.join(3000);
        hold.countDown();
        holder.join(3000);

        assertFalse(secondGotLock.get());
        assertTrue(entered.get() == 1);
    }

    @Test
    public void distributedFactory_tryLockSuccess_shouldConsumeAndUnlockBoth() {
        DistributedLockFactory factory = mock(DistributedLockFactory.class);
        Lock dLock = mock(Lock.class);
        when(factory.getLock(any())).thenReturn(dLock);
        when(dLock.tryLock()).thenReturn(true);

        LockSupportImpl lockSupport = new LockSupportImpl(factory);
        Pair<String, LockBizType> key = Pair.of("event-distributed", LockBizType.EVENT_HANDLE);
        AtomicBoolean consumed = new AtomicBoolean(false);

        assertTrue(lockSupport.consumeIfTryLock(key, () -> consumed.set(true)));
        assertTrue(consumed.get());
        verify(dLock).unlock();
    }

    @Test
    public void distributedFactory_tryLockFail_shouldReturnFalseAndReleaseLocal() {
        DistributedLockFactory factory = mock(DistributedLockFactory.class);
        Lock dLock = mock(Lock.class);
        when(factory.getLock(any())).thenReturn(dLock);
        when(dLock.tryLock()).thenReturn(false, true);

        LockSupportImpl lockSupport = new LockSupportImpl(factory);
        Pair<String, LockBizType> key = Pair.of("event-distributed-fail", LockBizType.EVENT_HANDLE);
        AtomicBoolean consumed = new AtomicBoolean(false);

        assertFalse(lockSupport.consumeIfTryLock(key, () -> consumed.set(true)));
        assertFalse(consumed.get());

        AtomicBoolean secondConsumed = new AtomicBoolean(false);
        assertTrue(lockSupport.consumeIfTryLock(key, () -> secondConsumed.set(true)));
        assertTrue(secondConsumed.get());
    }

    @Test
    public void differentKeys_shouldAllowConcurrentConsume() throws Exception {
        LockSupportImpl lockSupport = new LockSupportImpl(null);
        Pair<String, LockBizType> key1 = Pair.of("event-a", LockBizType.EVENT_HANDLE);
        Pair<String, LockBizType> key2 = Pair.of("event-b", LockBizType.EVENT_HANDLE);
        CountDownLatch bothStarted = new CountDownLatch(2);
        CountDownLatch release = new CountDownLatch(1);

        Thread t1 = new Thread(() -> lockSupport.consumeIfTryLock(key1, () -> {
            bothStarted.countDown();
            try {
                release.await(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
        Thread t2 = new Thread(() -> lockSupport.consumeIfTryLock(key2, () -> {
            bothStarted.countDown();
            try {
                release.await(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));

        t1.start();
        t2.start();
        assertTrue(bothStarted.await(3, TimeUnit.SECONDS));
        release.countDown();
        t1.join(3000);
        t2.join(3000);
    }

    @Test(expected = EasyEventException.class)
    public void consumeIfLock_whenCannotAcquire_shouldThrow() throws Exception {
        LockSupportImpl lockSupport = new LockSupportImpl(null);
        Pair<String, LockBizType> key = Pair.of("event-blocked", LockBizType.EVENT_HANDLE);
        CountDownLatch hold = new CountDownLatch(1);
        CountDownLatch acquired = new CountDownLatch(1);

        Thread holder = new Thread(() -> lockSupport.consumeIfTryLock(key, () -> {
            acquired.countDown();
            try {
                hold.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
        holder.start();
        assertTrue(acquired.await(3, TimeUnit.SECONDS));

        try {
            lockSupport.consumeIfLock(key, () -> {
            });
        } finally {
            hold.countDown();
            holder.join(3000);
        }
    }
}
