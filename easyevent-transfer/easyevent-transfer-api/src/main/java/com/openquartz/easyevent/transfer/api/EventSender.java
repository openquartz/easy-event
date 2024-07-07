package com.openquartz.easyevent.transfer.api;

import java.util.List;
import com.openquartz.easyevent.common.model.LifecycleBean;
import com.openquartz.easyevent.storage.identify.EventId;
import com.openquartz.easyevent.storage.model.EventBody;

/**
 * Event Sender
 *
 * @author svnee
 */
public interface EventSender extends LifecycleBean {

    /**
     * 发送
     *
     * @param event 事件
     * @return 是否发送成功
     */
    <T> boolean send(T event);

    /**
     * 批量发布
     *
     * @param eventList 事件集合
     * @return 发布消息
     */
    <T> boolean sendList(List<T> eventList);

    /**
     * 重试发送
     *
     * @param eventId 事件消息
     * @param eventBody event-body
     * @return 是否成功发送
     */
    <T> boolean retrySend(EventId eventId, EventBody<T> eventBody);
}
