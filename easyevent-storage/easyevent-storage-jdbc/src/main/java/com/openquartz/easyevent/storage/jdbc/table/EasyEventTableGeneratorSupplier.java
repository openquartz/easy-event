package com.openquartz.easyevent.storage.jdbc.table;


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import com.openquartz.easyevent.common.utils.CollectionUtils;
import com.openquartz.easyevent.common.utils.StringUtils;
import com.openquartz.easyevent.storage.jdbc.sharding.ShardingRouter;

/**
 * table formatter supplier
 *
 * @author svnee
 **/
public class EasyEventTableGeneratorSupplier {

    private static final String TABLE_BUS_EVENT_ENTITY_FORMAT_PATTERN = "{0}_bus_event_entity";

    /**
     * 表名前缀
     */
    private String prefix = "ee";
    private final ShardingRouter shardingRouter;

    public EasyEventTableGeneratorSupplier(String prefix, ShardingRouter shardingRouter) {
        if (StringUtils.isNotBlank(prefix)) {
            this.prefix = prefix;
        }
        this.shardingRouter = shardingRouter;
    }

    public String genBusEventEntityTable() {
        return MessageFormat.format(TABLE_BUS_EVENT_ENTITY_FORMAT_PATTERN, prefix);
    }

    public String genBusEventEntityTable(Long eventEntityId) {
        int shardingIndex = shardingRouter.sharding(eventEntityId);
        if (shardingIndex < 0) {
            return MessageFormat.format(TABLE_BUS_EVENT_ENTITY_FORMAT_PATTERN, prefix);
        } else {
            return MessageFormat.format(TABLE_BUS_EVENT_ENTITY_FORMAT_PATTERN, prefix) + "_" + shardingIndex;
        }
    }

    public List<String> genAllBusEventEntityTableList() {
        int sharding = shardingRouter.totalSharding();
        if (sharding < 0) {
            return CollectionUtils.newArrayList(MessageFormat.format(TABLE_BUS_EVENT_ENTITY_FORMAT_PATTERN, prefix));
        }
        List<String> result = new ArrayList<>();
        for (int i = 0; i < sharding; i++) {
            String shardingTable = MessageFormat.format(TABLE_BUS_EVENT_ENTITY_FORMAT_PATTERN, prefix) + "_" + i;
            result.add(shardingTable);
        }
        return result;
    }

}
