package org.svnee.easyevent.transfer.api.rocket;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.svnee.easyevent.common.transaction.TransactionSupport;
import org.svnee.easyevent.common.utils.CollectionUtils;
import org.svnee.easyevent.transfer.api.EventSender;
import org.svnee.easyevent.transfer.api.limiting.EventTransferSenderLimitingControl;
import org.svnee.easyevent.transfer.api.rocket.common.BatchSendResult;
import org.svnee.easyevent.transfer.api.rocket.common.RocketMqProducer;
import org.svnee.easyevent.storage.api.EventStorageService;
import org.svnee.easyevent.storage.identify.EventId;
import org.svnee.easyevent.storage.model.BaseEventEntity;

/**
 * RocketMQ发送
 *
 * @author svnee
 **/
@Slf4j
public class RocketMqEventSender implements EventSender {

    private final EventStorageService eventStorageService;
    private final ExecutorService asyncSendExecutor;
    private final TransactionSupport transactionSupport;
    private final RocketMqProducer rocketMqProducer;
    private final EventTransferSenderLimitingControl eventTransferSenderLimitingControl;

    public RocketMqEventSender(EventStorageService eventStorageService,
        ExecutorService asyncSendExecutor,
        TransactionSupport transactionSupport,
        RocketMqProducer rocketMqProducer,
        EventTransferSenderLimitingControl eventTransferSenderLimitingControl) {
        this.eventStorageService = eventStorageService;
        this.asyncSendExecutor = asyncSendExecutor;
        this.transactionSupport = transactionSupport;
        this.rocketMqProducer = rocketMqProducer;
        this.eventTransferSenderLimitingControl = eventTransferSenderLimitingControl;
    }

    @Override
    public void init() {
        rocketMqProducer.init();
    }

    @Override
    public void destroy() {
        rocketMqProducer.destroy();
    }

    @Override
    public <T> boolean send(T event) {

        EventId eventId = transactionSupport.execute(() -> eventStorageService.save(event));

        // 事务后触发发送
        transactionSupport.executeAfterCommit(() -> asyncSendExecutor.execute(() ->
            // 发送限流控制
            eventTransferSenderLimitingControl.control(event, eventId, (e, eId) -> {
                // 发送消息
                try {
                    rocketMqProducer.sendMessage(e, eId);
                } catch (Exception ex) {
                    // 存储执行
                    transactionSupport.execute(() -> {
                        eventStorageService.sendFailed(eId, ex);
                        return true;
                    });
                }
                // 存储执行
                transactionSupport.execute(() -> {
                    eventStorageService.sendComplete(eId);
                    return true;
                });
            })));
        return true;
    }

    @Override
    public <T> boolean sendList(List<T> eventList) {

        List<EventId> eventIdList = transactionSupport.execute(() -> eventStorageService.saveList(eventList));

        // 事务后触发发送
        transactionSupport.executeAfterCommit(() -> asyncSendExecutor.execute(() ->
            // 发送限流
            eventTransferSenderLimitingControl.control(eventList, eventIdList, (eList, eIdList) -> {
                // 发送消息
                BatchSendResult batchSendResult = rocketMqProducer.sendMessageList(eList, eIdList);

                if (CollectionUtils.isNotEmpty(batchSendResult.getSendCompletedIndexList())) {
                    List<EventId> completedEventIdList = batchSendResult.getSendCompletedIndexList()
                        .stream()
                        .map(eventIdList::get)
                        .collect(Collectors.toList());
                    // 存储执行
                    transactionSupport.execute(() -> {
                        eventStorageService.sendCompleteList(completedEventIdList);
                        return true;
                    });
                }
                if (CollectionUtils.isNotEmpty(batchSendResult.getSendFailedIndexList())) {
                    List<EventId> completedEventIdList = batchSendResult.getSendFailedIndexList()
                        .stream()
                        .map(eventIdList::get)
                        .collect(Collectors.toList());
                    // 存储执行
                    transactionSupport.execute(() -> {
                        eventStorageService.sendFailedList(completedEventIdList, batchSendResult.getFailedException());
                        return true;
                    });
                }
            })));
        return true;
    }

    @Override
    public <T> boolean retrySend(EventId eventId, T event) {

        BaseEventEntity eventEntity = eventStorageService.getBaseEntity(eventId);

        if (eventStorageService.isMoreThanMustTrigger(eventEntity) || eventEntity.isProcessComplete()) {
            log.warn("[RocketMqEventSender#retrySend] event-retry more than max-trigger!eventId:{}", eventId);
            return false;
        }

        asyncSendExecutor.execute(() -> eventTransferSenderLimitingControl.control(event, eventId, (e, eId) -> {
            // 发送消息
            try {
                rocketMqProducer.sendMessage(e, eId);
            } catch (Exception ex) {
                eventStorageService.sendFailed(eId, ex);
                return;
            }
            // 存储执行
            transactionSupport.execute(() -> {
                eventStorageService.sendComplete(eId);
                return true;
            });
        }));
        return false;
    }
}



