package com.openquartz.easyevent.transfer.api.adapter;

import java.util.List;
import com.openquartz.easyevent.storage.identify.EventId;
import com.openquartz.easyevent.storage.model.EventBody;
import com.openquartz.easyevent.transfer.api.common.BatchSendResult;

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
    <T> void sendMessage(EventBody<T> event, EventId eventId);

    /**
     * send message list
     *
     * @param eventList event list
     * @param eventIdList eventIdList
     * @param <T> T
     * @return send result
     */
    <T> BatchSendResult sendMessageList(List<EventBody<T>> eventList, List<EventId> eventIdList);

}
