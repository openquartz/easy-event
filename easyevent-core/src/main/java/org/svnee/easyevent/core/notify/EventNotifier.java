package org.svnee.easyevent.core.notify;

import java.util.List;
import org.svnee.easyevent.storage.model.BusEventEntity;

/**
 * event notifier
 *
 * @author svnee
 */
public interface EventNotifier {

    /**
     * notify event
     *
     * @param eventList eventList
     */
    void notify(List<BusEventEntity> eventList);
}
