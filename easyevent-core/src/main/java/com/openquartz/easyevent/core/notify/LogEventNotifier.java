package com.openquartz.easyevent.core.notify;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import com.openquartz.easyevent.common.constant.CommonConstants;
import com.openquartz.easyevent.common.utils.CollectionUtils;
import com.openquartz.easyevent.storage.model.BaseEventEntity;
import com.openquartz.easyevent.storage.model.BusEventEntity;

/**
 * Log event
 *
 * @author svnee
 **/
@Slf4j
public class LogEventNotifier implements EventNotifier {

    @Override
    public void notify(List<BusEventEntity> eventList) {
        if (CollectionUtils.isEmpty(eventList)) {
            return;
        }
        String eventIdList = eventList.stream()
            .map(BaseEventEntity::getEntityId)
            .map(String::valueOf)
            .collect(Collectors.joining(CommonConstants.COMMA));
        log.error("[LogEventNotifier]EventExecuteHandle,Error!more than max retry count!,eventId:{}", eventIdList);
    }
}
