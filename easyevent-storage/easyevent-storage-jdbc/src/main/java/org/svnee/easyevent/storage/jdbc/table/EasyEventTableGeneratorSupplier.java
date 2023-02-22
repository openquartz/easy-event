package org.svnee.easyevent.storage.jdbc.table;


import java.text.MessageFormat;
import org.svnee.easyevent.common.utils.StringUtils;

/**
 * table formatter supplier
 *
 * @author svnee
 **/
public final class EasyEventTableGeneratorSupplier {

    private EasyEventTableGeneratorSupplier() {
    }

    /**
     * 表名前缀
     */
    private static String prefix = "ee";

    private static final String TABLE_BUS_EVENT_ENTITY_FORMAT_PATTERN = "{0}_bus_event_entity";

    public static void setPrefix(String prefix) {
        if (StringUtils.isNotBlank(prefix)) {
            EasyEventTableGeneratorSupplier.prefix = prefix;
        }
    }

    public static String genBusEventEntityTable() {
        return MessageFormat.format(TABLE_BUS_EVENT_ENTITY_FORMAT_PATTERN, prefix);
    }
}
