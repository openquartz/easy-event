package com.openquartz.easyevent.storage.jdbc.sharding.property;

import lombok.Data;

/**
 * EventStorageShardingProperty
 * @author svnee
 **/
@Data
public class EventStorageShardingProperty {

    /**
     * 总分片下标
     */
    private int totalSharding = -1;

}
