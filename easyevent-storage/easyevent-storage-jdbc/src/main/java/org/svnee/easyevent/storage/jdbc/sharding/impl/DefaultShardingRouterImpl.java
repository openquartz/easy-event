package org.svnee.easyevent.storage.jdbc.sharding.impl;

import org.svnee.easyevent.common.utils.Asserts;
import org.svnee.easyevent.storage.exception.IdentifyErrorCode;
import org.svnee.easyevent.storage.identify.IdGenerator;
import org.svnee.easyevent.storage.jdbc.sharding.ShardingRouter;
import org.svnee.easyevent.storage.jdbc.sharding.property.EventStorageShardingProperty;


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
