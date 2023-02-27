package org.svnee.easyevent.storage.jdbc.sharding;

/**
 * sharding
 *
 * @author svnee
 */
public interface ShardingRouter {

    /**
     * 分片
     *
     * 如果不开启分片时,需要返回值小于0即可。否则返回的是下标
     *
     * @param eventEntityId entityId
     * @return sharding index
     */
    int sharding(Long eventEntityId);

    /**
     * totalSharding
     * @return totalSharding
     */
    int totalSharding();
}
