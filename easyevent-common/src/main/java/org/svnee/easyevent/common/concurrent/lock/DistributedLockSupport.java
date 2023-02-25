package org.svnee.easyevent.common.concurrent.lock;

import java.util.concurrent.locks.Lock;
import org.svnee.easyevent.common.model.Pair;

/**
 * Distributed EventLock
 *
 * @author svnee
 */
public interface DistributedLockSupport {

    /**
     * Get Lock
     *
     * @param lockKey lockKey
     * @return lock must not be null
     */
    Lock getLock(Pair<String, LockBizType> lockKey);

}
