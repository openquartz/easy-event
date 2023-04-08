package com.openquartz.easyevent.storage.jdbc.mapper;

import java.util.List;
import com.openquartz.easyevent.storage.identify.EventId;
import com.openquartz.easyevent.storage.model.BaseEventEntity;
import com.openquartz.easyevent.storage.model.BusEventEntity;
import com.openquartz.easyevent.storage.model.BusEventSelectorCondition;
import com.openquartz.easyevent.storage.model.EventLifecycleState;

/**
 * BusEventEntityMapper
 *
 * @author svnee
 */
public interface BusEventEntityMapper {

    /**
     * insert-selective
     *
     * @param busEventEntity busEventEntity
     */
    void insertSelective(BusEventEntity busEventEntity);

    /**
     * refresh source
     *
     * @param entityId entityId
     * @param sourceId sourceId
     */
    void refreshSourceId(Long entityId, Long sourceId);

    /**
     * insertList no id
     *
     * @param entityList entity
     */
    void insertList(List<BusEventEntity> entityList);

    /**
     * insertList with id
     *
     * @param entityList entity
     */
    void insertListWithSupplierId(List<BusEventEntity> entityList);

    /**
     * 刷新成发送成功了
     *
     * @param eventId eventId
     * @param transferSuccess 发送成功
     */
    void refreshSendComplete(EventId eventId, EventLifecycleState transferSuccess);

    /**
     * 批量更新发送成功
     *
     * @param eventIdList eventId
     * @param transferSuccess transfer-success
     */
    void batchRefreshSendComplete(List<EventId> eventIdList, EventLifecycleState transferSuccess);

    /**
     * send failed
     *
     * @param eventId eventId
     * @param transferFailed 发送失败
     * @param ex ex
     */
    void refreshSendFailed(EventId eventId, EventLifecycleState transferFailed, Exception ex);

    /**
     * 开始处理
     *
     * @param eventId eventId
     * @param startProcessing 处理状态
     */
    void refreshStartProcessing(EventId eventId, EventLifecycleState startProcessing);

    /**
     * 处理完成
     *
     * @param eventId eventId
     * @param processComplete 处理成功状态
     */
    void processingComplete(EventId eventId, EventLifecycleState processComplete);

    /**
     * process failed handle method
     *
     * @param eventId eventId
     * @param processFailed processFailed
     * @param successSubscriberIdentifyList success subscriber-identify list
     * @param invokeError invoke error
     */
    void processingFailed(EventId eventId, EventLifecycleState processFailed,
        List<String> successSubscriberIdentifyList, Exception invokeError);

    /**
     * get successfulSubscriber-Identify
     *
     * @param eventId eventId
     * @return subscriber-identify
     */
    List<String> getSuccessfulSubscriberIdentify(EventId eventId);

    /**
     * get base-entity
     *
     * @param eventId eventId
     * @return baseEventEntity
     */
    BaseEventEntity getBaseEntity(EventId eventId);

    /**
     * get bus-event-entity by selective condition
     *
     * @param condition condition
     * @return entity list
     */
    List<BusEventEntity> getBySelectiveCondition(BusEventSelectorCondition condition);
}
