package com.openquartz.easyevent.storage.jdbc.sharding.impl;

import com.openquartz.easyevent.common.utils.Asserts;
import com.openquartz.easyevent.storage.jdbc.sharding.ShardingRouter;
import com.openquartz.easyevent.storage.exception.IdentifyErrorCode;
import com.openquartz.easyevent.storage.identify.IdGenerator;
import com.openquartz.easyevent.storage.jdbc.sharding.property.EventStorageShardingProperty;


/**
 * DefaultShardingRouterImpl
 *
 * @author svnee
 **/
public class DefaultShardingRouterImpl implements ShardingRouter {

    private final EventStorageShardingProperty eventStorageShardingProperty;

    public DefaultShardingRouterImpl(EventStorageShardingProperty eventStorageShardingProperty,
        IdGenerator idGenerator) {
        this.eventStorageShardingProperty = eventStorageShardingProperty;
        if (eventStorageShardingProperty.getTotalSharding() > 0) {
            Asserts.notNull(idGenerator, IdentifyErrorCode.ENABLE_SHARDING_MUST_PROVIDER_IDENTIFY_GENERATOR);
        }
        Asserts.isTrue(eventStorageShardingProperty.getTotalSharding() != 0,
            IdentifyErrorCode.SHARDING_PROPERTY_ILLEGAL_ERROR);
    }

    @Override
    public int sharding(Long eventEntityId) {
        if (eventStorageShardingProperty.getTotalSharding() < 0) {
            return -1;
        }
        return (int) (eventEntityId % eventStorageShardingProperty.getTotalSharding());
    }

    @Override
    public int totalSharding() {
        return eventStorageShardingProperty.getTotalSharding();
    }
}
