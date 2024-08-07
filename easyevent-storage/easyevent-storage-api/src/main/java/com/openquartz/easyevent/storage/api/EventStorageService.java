package com.openquartz.easyevent.storage.api;

import java.util.List;
import com.openquartz.easyevent.storage.identify.EventId;
import com.openquartz.easyevent.storage.model.BaseEventEntity;
import com.openquartz.easyevent.storage.model.BusEventEntity;
import com.openquartz.easyevent.storage.model.BusEventSelectorCondition;
import com.openquartz.easyevent.storage.model.EventBody;

/**
 * 事件存储器
 *
 * @author svnee
 */
public interface EventStorageService {

    /**
     * 保存
     *
     * @param event 事件
     * @return eventId 事件ID
     */
    <T>EventId save(EventBody<T> event);

    /**
     * 批量保存
     *
     * @param eventList eventList
     * @return eventId 事件ID
     */
    <T>List<EventId> saveList(List<EventBody<T>> eventList);

    /**
     * 发送完成
     *
     * @param eventId 事件ID-发送完成
     */
    void sendComplete(EventId eventId);

    /**
     * 发送失败
     *
     * @param eventId eventId
     * @param ex send error
     */
    void sendFailed(EventId eventId, Throwable ex);

    /**
     * 发送完成
     *
     * @param eventIdList eventIdList
     */
    void sendCompleteList(List<EventId> eventIdList);

    /**
     * 发送失败
     *
     * @param eventIdList eventIdList
     * @param ex send error
     */
    void sendFailedList(List<EventId> eventIdList, Throwable ex);

    /**
     * getEvent
     *
     * @param eventId eventId
     * @return BaseEventEntity
     */
    BaseEventEntity getBaseEntity(EventId eventId);

    /**
     * 执行失败
     *
     * @param eventId eventID
     * @param ex ex
     */
    void processingFailed(EventId eventId, Throwable ex);

    /**
     * 执行失败
     *
     * @param eventId eventId
     * @param successSubscriberList 成功的订阅者
     * @param invokeError 执行失败异常
     */
    void processingFailed(EventId eventId, List<String> successSubscriberList, Throwable invokeError);

    /**
     * 执行成功
     *
     * @param eventId 事件ID
     */
    void processingCompleted(EventId eventId);

    /**
     * 是否已经大于最大触发次数
     *
     * @param eventEntity event
     * @return 是否已经大于
     */
    boolean isMoreThanMustTrigger(BaseEventEntity eventEntity);

    /**
     * 开启处理中
     *
     * @param eventId eventId
     * @return 是否可以处理
     */
    boolean startProcessing(EventId eventId);

    /**
     * 根据条件查询
     *
     * @param condition 条件
     * @return bus-event
     */
    List<BusEventEntity> get(BusEventSelectorCondition condition);

    /**
     * 是否已经存在
     * @param eventId eventId
     * @return 是否存在
     */
    boolean isPresent(Long eventId);
}
