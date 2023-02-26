package org.svnee.easyevent.common.concurrent.lock.impl;

import static org.svnee.easyevent.common.utils.ParamUtils.checkNotNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;
import org.svnee.easyevent.common.concurrent.lock.Consumer;
import org.svnee.easyevent.common.concurrent.lock.DistributedLockFactory;
import org.svnee.easyevent.common.concurrent.lock.LockBizType;
import org.svnee.easyevent.common.concurrent.lock.LockSupport;
import org.svnee.easyevent.common.exception.CommonErrorCode;
import org.svnee.easyevent.common.model.Pair;
import org.svnee.easyevent.common.utils.Asserts;

/**
 * LockSupportImpl
 *
 * @author svnee
 **/
@Slf4j
public class LockSupportImpl implements LockSupport {

    private final DistributedLockFactory distributedLockFactory;
    private final Map<String, Lock> localLockMap = new ConcurrentHashMap<>();

    public LockSupportImpl(DistributedLockFactory distributedLockFactory) {
        this.distributedLockFactory = distributedLockFactory;
    }

    public static final String LOCK_KEY_FORMATTER = "%s:%s";

    /**
     * consume if tryLock
     * first try lock local lock,second try lock distributed lock
     *
     * @param lockKey lockKey
     * @param consumer consumer function
     * @return if consume function
     */
    @Override
    public boolean consumeIfTryLock(Pair<String, LockBizType> lockKey, Consumer consumer) {

        checkNotNull(lockKey);
        checkNotNull(lockKey.getKey());
        checkNotNull(lockKey.getValue());
        checkNotNull(consumer);

        String identifyLockKey = String.format(LOCK_KEY_FORMATTER, lockKey.getValue().getCode(), lockKey.getKey());

        Lock lock = localLockMap.computeIfAbsent(identifyLockKey, k -> new ReentrantLock());
        if (lock.tryLock()) {
            try {
                if (Objects.isNull(distributedLockFactory)) {
                    consumer.consume();
                    return true;
                } else {
                    Lock dLock = distributedLockFactory.getLock(lockKey);
                    checkNotNull(dLock);
                    if (dLock.tryLock()) {
                        try {
                            consumer.consume();
                            return true;
                        } finally {
                            dLock.unlock();
                        }
                    }
                }
            } finally {
                lock.unlock();
                localLockMap.remove(identifyLockKey, lock);
            }
        }
        return false;
    }

    @Override
    public void consumeIfLock(Pair<String, LockBizType> lockKey, Consumer consumer) {
        boolean tryLock = consumeIfTryLock(lockKey, consumer);
        Asserts.isTrueIfLog(tryLock,
            () -> log.warn("[LockSupportImpl#consumeIfLock] cannot Acquire Lock,lockKey:{}", lockKey),
            CommonErrorCode.CANNOT_GET_LOCK_ERROR);
    }
}
