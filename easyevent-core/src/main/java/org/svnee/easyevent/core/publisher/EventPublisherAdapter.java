package org.svnee.easyevent.core.publisher;

import static org.svnee.easyevent.common.utils.ParamUtils.checkNotNull;

import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.svnee.easyevent.common.utils.CollectionUtils;
import org.svnee.easyevent.core.EventBus;
import org.svnee.easyevent.common.concurrent.TraceThreadPoolExecutor;
import org.svnee.easyevent.core.dispatcher.DispatchInvokeResult;
import org.svnee.easyevent.core.intreceptor.PublisherInterceptorChain;
import org.svnee.easyevent.core.intreceptor.PublisherInterceptorContext;
import org.svnee.easyevent.transfer.api.EventSender;

/**
 * @author svnee
 **/
public abstract class EventPublisherAdapter implements EventPublisher {

    /**
     * 直接调度触发EventBus
     *
     * @return event-bus
     */
    public abstract EventBus getDirectEventBus();

    /**
     * 获取事件发送
     *
     * @return 事件发送者
     */
    public abstract EventSender getEventSender();

    /**
     * 异步执行的线程池
     */
    public TraceThreadPoolExecutor getExecutor() {
        return new TraceThreadPoolExecutor(10, 10, 100, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
    }

    @Override
    public <T> boolean syncPublish(T event) {

        PublisherInterceptorContext context = new PublisherInterceptorContext();
        boolean publish = PublisherInterceptorChain.applyPrePublish(event, context);
        if (!publish) {
            return true;
        }

        PublisherInterceptorChain.triggerAfterCompletion(event, context, null);

        // 触发EventBus
        EventBus directEventBus = getDirectEventBus();

        checkNotNull(directEventBus);

        DispatchInvokeResult invokeResult = directEventBus.post(event);
        return invokeResult.isSuccess();
    }

    @Override
    public <T> boolean asyncPublish(T event) {

        PublisherInterceptorContext publishContext = new PublisherInterceptorContext();
        boolean publish = PublisherInterceptorChain.applyPrePublish(event, publishContext);
        if (!publish) {
            return true;
        }
        boolean sendResult;
        try {
            sendResult = getEventSender().send(event);
        } catch (Exception ex) {
            PublisherInterceptorChain.triggerAfterCompletion(event, publishContext, ex);
            throw ex;
        }
        PublisherInterceptorChain.triggerAfterCompletion(event, publishContext, null);
        return sendResult;
    }

    @Override
    public <T> boolean asyncPublishList(List<T> eventList) {

        PublisherInterceptorContext publishContext = new PublisherInterceptorContext();

        List<Object> filterEventList = eventList.stream()
            .filter(event -> PublisherInterceptorChain.applyPrePublish(event, publishContext))
            .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(filterEventList)) {
            return true;
        }

        boolean sendResult;
        try {
            sendResult = getEventSender().sendList(filterEventList);
        } catch (Exception ex) {
            for (Object event : filterEventList) {
                PublisherInterceptorChain.triggerAfterCompletion(event, publishContext, ex);
            }
            throw ex;
        }
        for (Object event : filterEventList) {
            PublisherInterceptorChain.triggerAfterCompletion(event, publishContext, null);
        }
        return sendResult;
    }
}
