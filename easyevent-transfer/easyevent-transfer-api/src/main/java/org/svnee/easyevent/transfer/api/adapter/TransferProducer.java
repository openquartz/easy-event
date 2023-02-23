package org.svnee.easyevent.transfer.api.adapter;

import java.util.List;
import org.svnee.easyevent.storage.identify.EventId;
import org.svnee.easyevent.transfer.api.common.BatchSendResult;

/**
 * producer
 *
 * @author svnee
 */
public interface TransferProducer {

    /**
     * send message
     *
     * @param event event
     * @param eventId eventId
     * @param <T> T
     */
    <T> void sendMessage(T event, EventId eventId);

    /**
     * send message list
     *
     * @param eventList event list
     * @param eventIdList eventIdList
     * @param <T> T
     * @return send result
     */
    <T> BatchSendResult sendMessageList(List<T> eventList, List<EventId> eventIdList);

}
