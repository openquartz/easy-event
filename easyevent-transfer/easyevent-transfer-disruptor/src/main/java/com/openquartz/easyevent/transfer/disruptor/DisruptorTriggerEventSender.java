package com.openquartz.easyevent.transfer.disruptor;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.IgnoreExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import com.openquartz.easyevent.common.concurrent.TraceThreadPoolExecutor;
import com.openquartz.easyevent.common.concurrent.lock.LockSupport;
import com.openquartz.easyevent.common.serde.Serializer;
import com.openquartz.easyevent.common.transaction.TransactionSupport;
import com.openquartz.easyevent.storage.api.EventStorageService;
import com.openquartz.easyevent.storage.identify.EventId;
import com.openquartz.easyevent.storage.model.BaseEventEntity;
import com.openquartz.easyevent.transfer.api.EventSender;
import com.openquartz.easyevent.transfer.api.message.EventMessage;
import com.openquartz.easyevent.transfer.disruptor.common.DisruptorTriggerEventTranslator;
import com.openquartz.easyevent.transfer.disruptor.event.DisruptorTriggerEvent;
import com.openquartz.easyevent.transfer.disruptor.factory.DisruptorTriggerEventFactory;
import com.openquartz.easyevent.transfer.disruptor.factory.DisruptorTriggerThreadFactory;
import com.openquartz.easyevent.transfer.disruptor.property.DisruptorTriggerProperty;

/**
 * Disruptor Sender Trigger Event
 *
 * @author svnee
 **/
@Slf4j
public class DisruptorTriggerEventSender implements EventSender {

    private static final AtomicLong INDEX = new AtomicLong(1);

    private final Disruptor<DisruptorTriggerEvent> disruptor;
    private final Serializer serializer;
    private final TransactionSupport transactionSupport;
    private final EventStorageService eventStorageService;

    public DisruptorTriggerEventSender(DisruptorTriggerProperty property,
        Consumer<EventMessage> eventHandler,
        Serializer serializer,
        TransactionSupport transactionSupport,
        EventStorageService eventStorageService,
        LockSupport lockSupport) {

        this.serializer = serializer;
        this.transactionSupport = transactionSupport;
        this.eventStorageService = eventStorageService;

        this.disruptor = new Disruptor<>(new DisruptorTriggerEventFactory(),
            property.getConsumerProperty().getBufferSize(), runnable -> {
            return new Thread(new ThreadGroup(property.getDisruptorThreadGroup()), runnable,
                property.getDisruptorThreadPrefix() + INDEX.getAndIncrement());
        }, ProducerType.MULTI, new BlockingWaitStrategy());

        final ExecutorService executor = new TraceThreadPoolExecutor(property.getConsumerProperty().getCorePoolSize(),
            property.getConsumerProperty().getMaximumPoolSize(),
            property.getConsumerProperty().getKeepAliveTime(),
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(property.getConsumerProperty().getBufferSize()),
            DisruptorTriggerThreadFactory.create(property.getConsumerProperty().getThreadPrefix(), false),
            new ThreadPoolExecutor.AbortPolicy());

        DisruptorTriggerEventHandler[] consumers = new DisruptorTriggerEventHandler[property.getConsumerProperty()
            .getMaximumPoolSize()];
        for (int i = 0; i < property.getConsumerProperty().getMaximumPoolSize(); i++) {
            consumers[i] = new DisruptorTriggerEventHandler(eventHandler, executor, lockSupport);
        }
        disruptor.handleEventsWithWorkerPool(consumers);
        disruptor.setDefaultExceptionHandler(new IgnoreExceptionHandler());
        disruptor.start();
    }


    /**
     * publish disruptor event.
     */
    public <T> void publishEvent(T event, EventId eventId) {
        final RingBuffer<DisruptorTriggerEvent> ringBuffer = disruptor.getRingBuffer();
        Class<?> eventType = event.getClass();

        EventMessage eventMessage = new EventMessage();
        eventMessage.setEventId(eventId);
        eventMessage.setClassName(eventType.getName());
        eventMessage.setEventData(serializer.serialize(event));

        ringBuffer.publishEvent(new DisruptorTriggerEventTranslator(), eventMessage);
    }


    @Override
    public <T> boolean send(T event) {

        EventId eventId = transactionSupport.execute(() -> eventStorageService.save(event));

        // 事务后触发发送
        transactionSupport.executeAfterCommit(() -> {
            try {
                publishEvent(event, eventId);
                // 存储执行
                transactionSupport.execute(() -> {
                    eventStorageService.sendComplete(eventId);
                    return true;
                });
            } catch (Exception ex) {
                log.error("[DisruptorTriggerEventSender#send] publish-error!eventId:{},event:{}", eventId, event, ex);
            }
        });

        return false;
    }

    @Override
    public <T> boolean sendList(List<T> eventList) {
        List<EventId> eventIdList = transactionSupport.execute(() -> eventStorageService.saveList(eventList));

        // 事务后触发发送
        transactionSupport.executeAfterCommit(() -> {

            for (int i = 0; i < eventList.size(); i++) {
                T event = eventList.get(i);
                EventId eventId = eventIdList.get(i);
                try {
                    publishEvent(event, eventId);
                    // 存储执行
                    transactionSupport.execute(() -> {
                        eventStorageService.sendComplete(eventId);
                        return true;
                    });
                } catch (Exception ex) {
                    log.error("[DisruptorTriggerEventSender#sendList] publish-error!eventId:{},event:{}", eventId,
                        event,
                        ex);
                }
            }
        });

        return false;
    }

    @Override
    public <T> boolean retrySend(EventId eventId, T event) {
        BaseEventEntity eventEntity = eventStorageService.getBaseEntity(eventId);

        if (eventStorageService.isMoreThanMustTrigger(eventEntity)) {
            log.warn("[RocketMqEventSender#retrySend] event-retry more than max-trigger!eventId:{}", eventId);
            return false;
        }
        // 发送消息
        try {
            publishEvent(event, eventId);
        } catch (Exception ex) {
            eventStorageService.sendFailed(eventId, ex);
            return false;
        }
        // 存储执行
        transactionSupport.execute(() -> {
            eventStorageService.sendComplete(eventId);
            return true;
        });
        return false;
    }

    @Override
    public void destroy() {
        disruptor.shutdown();
    }
}
