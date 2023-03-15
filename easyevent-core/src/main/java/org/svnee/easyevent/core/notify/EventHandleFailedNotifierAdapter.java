package org.svnee.easyevent.core.notify;

import static org.svnee.easyevent.storage.model.EventLifecycleState.AVAILABLE;
import static org.svnee.easyevent.storage.model.EventLifecycleState.IN_PROCESSING;
import static org.svnee.easyevent.storage.model.EventLifecycleState.PROCESS_FAILED;
import static org.svnee.easyevent.storage.model.EventLifecycleState.TRANSFER_FAILED;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.svnee.easyevent.common.utils.CollectionUtils;
import org.svnee.easyevent.common.utils.DateUtils;
import org.svnee.easyevent.storage.api.EventStorageService;
import org.svnee.easyevent.storage.model.BusEventEntity;
import org.svnee.easyevent.storage.model.BusEventSelectorCondition;
import org.svnee.easyevent.storage.model.EventLifecycleState;

/**
 * EventHandleFailedNotifierAdapter
 *
 * @author svnee
 **/
public abstract class EventHandleFailedNotifierAdapter implements EventHandleFailedNotifier {

    private final EventStorageService eventStorageService;
    private final Integer maxErrorCount;
    private final EventNotifier notifier;

    public EventHandleFailedNotifierAdapter(EventStorageService eventStorageService, Integer maxErrorCount,
        EventNotifier notifier) {
        this.eventStorageService = eventStorageService;
        this.maxErrorCount = maxErrorCount;
        this.notifier = notifier;
    }

    @Override
    public void handle() {

        Date now = new Date();

        BusEventSelectorCondition condition = BusEventSelectorCondition.builder(50)
            .minErrorCount(maxErrorCount)
            .lifecycleState(CollectionUtils.newArrayList(AVAILABLE, PROCESS_FAILED, IN_PROCESSING, TRANSFER_FAILED))
            .start(DateUtils.addHours(now, -2))
            .end(DateUtils.addMinutes(now, -5));
        List<BusEventEntity> entityList = eventStorageService.get(condition);
        if (CollectionUtils.isEmpty(entityList)) {
            return;
        }
        notifier.notify(entityList);
    }
}
