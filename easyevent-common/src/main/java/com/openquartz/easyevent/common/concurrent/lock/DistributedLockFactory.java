package com.openquartz.easyevent.common.concurrent.lock;

import java.util.concurrent.locks.Lock;
import com.openquartz.easyevent.common.model.Pair;

/**
 * Distributed EventLock
 *
 * @author svnee
 */
public interface DistributedLockFactory {

    /**
     * Get Lock
     *
     * @param lockKey lockKey
     * @return lock must not be null
     */
    Lock getLock(Pair<String, LockBizType> lockKey);

}
