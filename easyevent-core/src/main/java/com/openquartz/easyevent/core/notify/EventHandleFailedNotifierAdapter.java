package com.openquartz.easyevent.core.notify;

import static com.openquartz.easyevent.storage.model.EventLifecycleState.AVAILABLE;
import static com.openquartz.easyevent.storage.model.EventLifecycleState.IN_PROCESSING;
import static com.openquartz.easyevent.storage.model.EventLifecycleState.PROCESS_FAILED;
import static com.openquartz.easyevent.storage.model.EventLifecycleState.TRANSFER_FAILED;

import java.util.Date;
import java.util.List;

import com.openquartz.easyevent.common.property.EasyEventProperties;
import com.openquartz.easyevent.common.utils.CollectionUtils;
import com.openquartz.easyevent.common.utils.DateUtils;
import com.openquartz.easyevent.storage.api.EventStorageService;
import com.openquartz.easyevent.storage.model.BusEventEntity;
import com.openquartz.easyevent.storage.model.BusEventSelectorCondition;

/**
 * EventHandleFailedNotifierAdapter
 *
 * @author svnee
 **/
public abstract class EventHandleFailedNotifierAdapter implements EventHandleFailedNotifier {

    private final EasyEventProperties easyEventProperties;
    private final EventStorageService eventStorageService;
    private final Integer maxErrorCount;
    private final EventNotifier notifier;

    public EventHandleFailedNotifierAdapter(EasyEventProperties easyEventProperties,
                                            EventStorageService eventStorageService,
                                            Integer maxErrorCount,
                                            EventNotifier notifier) {
        this.easyEventProperties = easyEventProperties;
        this.eventStorageService = eventStorageService;
        this.maxErrorCount = maxErrorCount;
        this.notifier = notifier;
    }

    @Override
    public void handle() {

        Date now = new Date();

        BusEventSelectorCondition condition = BusEventSelectorCondition.builder(easyEventProperties.getAppId(), 50)
                .minErrorCount(maxErrorCount)
                .lifecycleState(CollectionUtils.newArrayList(AVAILABLE, PROCESS_FAILED, IN_PROCESSING, TRANSFER_FAILED))
                .start(DateUtils.addHours(now, -2))
                .end(DateUtils.addMinutes(now, -10));
        List<BusEventEntity> entityList = eventStorageService.get(condition);
        if (CollectionUtils.isEmpty(entityList)) {
            return;
        }
        notifier.notify(entityList);
    }
}
