package org.svnee.easyevent.core.trigger;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.svnee.easyevent.common.exception.EasyEventException;
import org.svnee.easyevent.common.property.EasyEventProperties;
import org.svnee.easyevent.common.serde.Serializer;
import org.svnee.easyevent.common.transaction.TransactionSupport;
import org.svnee.easyevent.common.utils.ExceptionUtils;
import org.svnee.easyevent.core.EventBus;
import org.svnee.easyevent.core.Subscriber;
import org.svnee.easyevent.core.dispatcher.DispatchInvokeResult;
import org.svnee.easyevent.core.exception.EventBusErrorCode;
import org.svnee.easyevent.core.intreceptor.TriggerInterceptorChain;
import org.svnee.easyevent.core.intreceptor.TriggerInterceptorContext;
import org.svnee.easyevent.storage.api.EventStorageService;
import org.svnee.easyevent.storage.model.BaseEventEntity;
import org.svnee.easyevent.transfer.api.message.EventMessage;

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

        TriggerInterceptorContext context = new TriggerInterceptorContext();
        boolean preTrigger = TriggerInterceptorChain.applyPreTrigger(eventMessage, context);
        if (!preTrigger) {
            return;
        }

        // 开启处理
        String eventData = eventMessage.getEventData();
        Object event = null;
        try {
            event = getSerializer().deserialize(Class.forName(eventMessage.getClassName()), eventData);
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
            Object finalEvent = event;
            executeSuccess = getTransactionSupport().execute(() -> {
                DispatchInvokeResult invokeResult = getHandleEventBus()
                    .postAll(finalEvent, entity.getSuccessfulSubscriberList());
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
    }
}
