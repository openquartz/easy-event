package com.openquartz.easyevent.transfer.api.limiting;

import java.util.List;
import java.util.function.BiConsumer;
import com.openquartz.easyevent.storage.identify.EventId;

/**
 * EventTransfer Sender Limiting Control
 *
 * @author svnee
 */
public interface EventTransferSenderLimitingControl {

    /**
     * control event handle
     * if limiting blocked throw {@link LimitingBlockedException}
     *
     * @param event event content
     * @param eventId eventId
     * @param senderConsumer sender function
     */
    <T> void control(T event, EventId eventId, BiConsumer<T, EventId> senderConsumer);

    /**
     * control event handle
     * if limiting blocked throw {@link LimitingBlockedException}
     *
     * @param eventList eventList
     * @param eventIdList eventIdList
     * @param batchSenderConsumer batch sender function
     */
    <T> void control(List<T> eventList, List<EventId> eventIdList,
        BiConsumer<List<T>, List<EventId>> batchSenderConsumer);
}
