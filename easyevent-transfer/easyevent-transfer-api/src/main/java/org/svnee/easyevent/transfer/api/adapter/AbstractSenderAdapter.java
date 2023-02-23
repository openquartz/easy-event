package org.svnee.easyevent.transfer.api.adapter;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.svnee.easyevent.common.transaction.TransactionSupport;
import org.svnee.easyevent.common.utils.CollectionUtils;
import org.svnee.easyevent.storage.api.EventStorageService;
import org.svnee.easyevent.storage.identify.EventId;
import org.svnee.easyevent.storage.model.BaseEventEntity;
import org.svnee.easyevent.transfer.api.EventSender;
import org.svnee.easyevent.transfer.api.common.BatchSendResult;
import org.svnee.easyevent.transfer.api.limiting.EventTransferSenderLimitingControl;

/**
 * Sender Adapter
 *
 * @author svnee
 **/
@Slf4j
public abstract class AbstractSenderAdapter implements EventSender {

    /**
     * Transfer Producer
     *
     * @return TransferProducer
     */
    public abstract TransferProducer getTransferProducer();

    /**
     * Transaction Support
     *
     * @return TransactionSupport
     */
    public abstract TransactionSupport getTransactionSupport();

    /**
     * Event Storage Service
     *
     * @return EventStorageService
     */
    public abstract EventStorageService getEventStorageService();

    /**
     * EventTransferTriggerLimitingControl
     *
     * @return EventTransferSenderLimitingControl
     */
    public abstract EventTransferSenderLimitingControl getEventTransferSenderLimitingControl();

    /**
     * AsyncSendExecutor
     *
     * @return AsyncSendExecutor
     */
    public abstract Executor getAsyncSendExecutor();

    @Override
    public <T> boolean send(T event) {

        EventId eventId = getTransactionSupport().execute(() -> getEventStorageService().save(event));

        // 事务后触发发送
        getTransactionSupport().executeAfterCommit(() -> getAsyncSendExecutor().execute(() ->
            // 发送限流控制
            getEventTransferSenderLimitingControl().control(event, eventId, (e, eId) -> {
                // 发送消息
                try {
                    getTransferProducer().sendMessage(e, eId);
                } catch (Exception ex) {
                    // 存储执行
                    getTransactionSupport().execute(() -> {
                        getEventStorageService().sendFailed(eId, ex);
                        return true;
                    });
                }
                // 存储执行
                getTransactionSupport().execute(() -> {
                    getEventStorageService().sendComplete(eId);
                    return true;
                });
            })));
        return true;
    }

    @Override
    public <T> boolean sendList(List<T> eventList) {

        List<EventId> eventIdList = getTransactionSupport().execute(() -> getEventStorageService().saveList(eventList));

        // 事务后触发发送
        getTransactionSupport().executeAfterCommit(() -> getAsyncSendExecutor().execute(() ->
            // 发送限流
            getEventTransferSenderLimitingControl().control(eventList, eventIdList, (eList, eIdList) -> {
                // 发送消息
                BatchSendResult batchSendResult = getTransferProducer().sendMessageList(eList, eIdList);

                if (CollectionUtils.isNotEmpty(batchSendResult.getSendCompletedIndexList())) {
                    List<EventId> completedEventIdList = batchSendResult.getSendCompletedIndexList()
                        .stream()
                        .map(eventIdList::get)
                        .collect(Collectors.toList());
                    // 存储执行
                    getTransactionSupport().execute(() -> {
                        getEventStorageService().sendCompleteList(completedEventIdList);
                        return true;
                    });
                }
                if (CollectionUtils.isNotEmpty(batchSendResult.getSendFailedIndexList())) {
                    List<EventId> completedEventIdList = batchSendResult.getSendFailedIndexList()
                        .stream()
                        .map(eventIdList::get)
                        .collect(Collectors.toList());
                    // 存储执行
                    getTransactionSupport().execute(() -> {
                        getEventStorageService()
                            .sendFailedList(completedEventIdList, batchSendResult.getFailedException());
                        return true;
                    });
                }
            })));
        return true;
    }

    @Override
    public <T> boolean retrySend(EventId eventId, T event) {

        BaseEventEntity eventEntity = getEventStorageService().getBaseEntity(eventId);

        if (getEventStorageService().isMoreThanMustTrigger(eventEntity) || eventEntity.isProcessComplete()) {
            log.warn("[RocketMqEventSender#retrySend] event-retry more than max-trigger!eventId:{}", eventId);
            return false;
        }

        getAsyncSendExecutor()
            .execute(() -> getEventTransferSenderLimitingControl().control(event, eventId, (e, eId) -> {
                // 发送消息
                try {
                    getTransferProducer().sendMessage(e, eId);
                } catch (Exception ex) {
                    getEventStorageService().sendFailed(eId, ex);
                    return;
                }
                // 存储执行
                getTransactionSupport().execute(() -> {
                    getEventStorageService().sendComplete(eId);
                    return true;
                });
            }));
        return false;
    }

}
