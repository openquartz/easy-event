package org.svnee.easyevent.transfer.api.limiting.impl;

import java.util.List;
import java.util.function.BiConsumer;
import lombok.extern.slf4j.Slf4j;
import org.svnee.easyevent.storage.identify.EventId;
import org.svnee.easyevent.transfer.api.limiting.EventTransferSenderLimitingControl;

/**
 * Default EventTransferSenderLimitingControl
 *
 * @author svnee
 **/
@Slf4j
public class DefaultEventTransferSenderLimitingControl implements EventTransferSenderLimitingControl {

    @Override
    public <T> void control(T event, EventId eventId, BiConsumer<T, EventId> senderConsumer) {
        senderConsumer.accept(event, eventId);
    }

    @Override
    public <T> void control(List<T> eventList, List<EventId> eventIdList,
        BiConsumer<List<T>, List<EventId>> batchSenderConsumer) {
        batchSenderConsumer.accept(eventList, eventIdList);
    }
}
