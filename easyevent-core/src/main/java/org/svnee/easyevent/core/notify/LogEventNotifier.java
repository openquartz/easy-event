package org.svnee.easyevent.core.notify;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.svnee.easyevent.common.constant.CommonConstants;
import org.svnee.easyevent.common.utils.CollectionUtils;
import org.svnee.easyevent.storage.model.BaseEventEntity;
import org.svnee.easyevent.storage.model.BusEventEntity;

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
