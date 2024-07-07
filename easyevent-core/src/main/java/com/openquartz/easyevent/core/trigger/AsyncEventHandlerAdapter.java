package com.openquartz.easyevent.core.trigger;

import com.openquartz.easyevent.core.EventBus;
import com.openquartz.easyevent.core.Subscriber;
import com.openquartz.easyevent.core.dispatcher.DispatchInvokeResult;

import java.util.List;
import java.util.stream.Collectors;

import com.openquartz.easyevent.storage.model.EventBody;
import com.openquartz.easyevent.storage.model.EventContext;
import lombok.extern.slf4j.Slf4j;
import com.openquartz.easyevent.common.concurrent.TraceContext;
import com.openquartz.easyevent.common.exception.EasyEventException;
import com.openquartz.easyevent.common.property.EasyEventProperties;
import com.openquartz.easyevent.common.serde.Serializer;
import com.openquartz.easyevent.common.transaction.TransactionSupport;
import com.openquartz.easyevent.common.utils.ExceptionUtils;
import com.openquartz.easyevent.core.exception.EventBusErrorCode;
import com.openquartz.easyevent.core.intreceptor.TriggerInterceptorChain;
import com.openquartz.easyevent.core.intreceptor.TriggerInterceptorContext;
import com.openquartz.easyevent.storage.api.EventStorageService;
import com.openquartz.easyevent.storage.model.BaseEventEntity;
import com.openquartz.easyevent.transfer.api.message.EventMessage;

/**
 * 事件触发执行器Adapter
 *
 * @author svnee
 **/
@Slf4j
public abstract class AsyncEventHandlerAdapter implements AsyncEventHandler {

    /**
     * 获取处理EventBus
     *
     * @return EventBus
     */
    public abstract EventBus getHandleEventBus();

    /**
     * 获取序列化
     *
     * @return 序列化
     */
    public abstract Serializer getSerializer();

    /**
     * 获取Event存储事件
     *
     * @return event 存储服务
     */
    public abstract EventStorageService getEventStorageService();

    /**
     * 事务支持
     *
     * @return 事务支持
     */
    public abstract TransactionSupport getTransactionSupport();

    /**
     * easy event properties
     *
     * @return properties
     */
    public abstract EasyEventProperties getEasyEventProperties();

    @Override
    public void handle(EventMessage eventMessage) {

        BaseEventEntity entity = getEventStorageService().getBaseEntity(eventMessage.getEventId());
        if (entity.isProcessComplete() || entity.getErrorCount() >= getEasyEventProperties().getMaxRetryCount()) {
            return;
        }
        try {

            // put traceId
            TraceContext.putSourceEventIdIfAbsent(eventMessage.getEventId().getId());

            TriggerInterceptorContext context = new TriggerInterceptorContext();
            boolean preTrigger = TriggerInterceptorChain.applyPreTrigger(eventMessage, context);
            if (!preTrigger) {
                return;
            }

            // 开启处理
            String eventData = eventMessage.getEventData();
            EventBody<?> eventBody = null;
            try {
                eventBody = getSerializer().deserialize(EventBody.class, eventData);
            } catch (Exception ex) {
                log.error("[EventTriggerAdapter#trigger]deserialize-error!eventMessage:{}", eventMessage, ex);
                getEventStorageService().processingFailed(eventMessage.getEventId(), ex);
                // trigger-complete
                TriggerInterceptorChain.triggerAfterCompletion(eventMessage, context, ex);
                ExceptionUtils.rethrow(ex);
            }
            boolean executeSuccess;
            try {
                // 执行EventBus.
                boolean process = getEventStorageService().startProcessing(eventMessage.getEventId());
                if (!process) {
                    log.error("[EventTriggerAdapter#trigger]start-process-failed!event:{}", eventMessage);
                    return;
                }
                EventBody<?> finalEventBody = eventBody;
                EventContext.set(finalEventBody.getContext());

                // execute the no join transaction subscribers
                DispatchInvokeResult noJoinTransactionInvokeResult = getHandleEventBus()
                        .postAll(finalEventBody.getEvent(), entity.getSuccessfulSubscriberList(), false);

                executeSuccess = getTransactionSupport().execute(() -> {
                    // execute the join transaction subscribers
                    DispatchInvokeResult joinTransactionInvokeResult = getHandleEventBus()
                            .postAll(finalEventBody.getEvent(), entity.getSuccessfulSubscriberList(), true);

                    DispatchInvokeResult invokeResult = noJoinTransactionInvokeResult.merge(joinTransactionInvokeResult);
                    List<String> successfulSubIdentifyList = invokeResult.getSuccessSubscriberList()
                            .stream()
                            .map(Subscriber::getTargetIdentify)
                            .collect(Collectors.toList());

                    // 未执行成功
                    if (!invokeResult.isSuccess()) {
                        getEventStorageService()
                                .processingFailed(eventMessage.getEventId(), successfulSubIdentifyList,
                                        invokeResult.getInvokeError());
                    } else {
                        getEventStorageService()
                                .processingCompleted(eventMessage.getEventId());
                    }
                    return invokeResult.isSuccess();
                });
            } catch (Exception ex) {
                log.error("[EventTriggerAdapter#trigger]exe-error!eventMessage:{}", eventMessage, ex);
                getEventStorageService().processingFailed(eventMessage.getEventId(), ex);
                executeSuccess = false;
                // trigger complete
                TriggerInterceptorChain.triggerAfterCompletion(eventMessage, context, ex);
                ExceptionUtils.rethrow(ex);
            }

            if (executeSuccess) {
                // trigger-complete
                TriggerInterceptorChain.triggerAfterCompletion(eventMessage, context, null);
            } else {
                log.error("[EventTriggerAdapter#trigger]execute-process-failed!event:{}", eventMessage);
                Exception ex = new EasyEventException(EventBusErrorCode.EVENT_HANDLE_ERROR);
                // trigger-complete
                TriggerInterceptorChain.triggerAfterCompletion(eventMessage, context, ex);
                ExceptionUtils.rethrow(ex);
            }
        } finally {
            TraceContext.clearSourceEventId();
            EventContext.remove();
        }
    }
}
