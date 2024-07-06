package com.openquartz.easyevent.core.compensate;

import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotNull;

import com.openquartz.easyevent.core.compensate.translator.EventCompensateParamTranslator;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import com.openquartz.easyevent.common.concurrent.lock.LockBizType;
import com.openquartz.easyevent.common.concurrent.lock.LockSupport;
import com.openquartz.easyevent.common.model.Pair;
import com.openquartz.easyevent.common.serde.Serializer;
import com.openquartz.easyevent.common.utils.CollectionUtils;
import com.openquartz.easyevent.common.utils.ExceptionUtils;
import com.openquartz.easyevent.core.trigger.AsyncEventHandler;
import com.openquartz.easyevent.storage.api.EventStorageService;
import com.openquartz.easyevent.storage.model.BaseEventEntity;
import com.openquartz.easyevent.storage.model.BusEventEntity;
import com.openquartz.easyevent.storage.model.BusEventSelectorCondition;
import com.openquartz.easyevent.storage.model.EventLifecycleState;
import com.openquartz.easyevent.transfer.api.EventSender;
import com.openquartz.easyevent.transfer.api.message.EventMessage;

/**
 * EventBusCompensateAdapter
 *
 * @author svnee
 **/
@Slf4j
public class EventCompensateServiceImpl implements EventCompensateService {

    private final EventStorageService storageService;
    private final AsyncEventHandler asyncEventHandler;
    private final Executor compensateExecutor;
    private final LockSupport lockSupport;
    private final EventSender eventSender;
    private final Serializer serializer;

    public EventCompensateServiceImpl(EventStorageService storageService,
                                      AsyncEventHandler asyncEventHandler,
                                      Executor compensateExecutor,
                                      LockSupport lockSupport,
                                      EventSender eventSender,
                                      Serializer serializer) {

        checkNotNull(storageService);
        checkNotNull(asyncEventHandler);
        checkNotNull(compensateExecutor);
        checkNotNull(lockSupport);
        checkNotNull(eventSender);
        checkNotNull(serializer);

        this.storageService = storageService;
        this.asyncEventHandler = asyncEventHandler;
        this.compensateExecutor = compensateExecutor;
        this.lockSupport = lockSupport;
        this.eventSender = eventSender;
        this.serializer = serializer;
    }

    @Override
    public void compensate(EventCompensateParam param) {

        if (log.isDebugEnabled()) {
            log.info("[EventCompensateServiceAdapter#compensate] start!param:{}", param);
        }

        BusEventSelectorCondition condition = EventCompensateParamTranslator.translate(param);
        List<BusEventEntity> busEventEntityList = storageService.get(condition);
        List<BusEventEntity> entityList = busEventEntityList.stream()
                .filter(e -> !e.isProcessComplete())
                .sorted((Comparator.comparing(BaseEventEntity::getEntityId)))
                .collect(Collectors.toList());

        doCompensate(entityList);

        if (log.isDebugEnabled()) {
            log.info("[EventCompensateServiceAdapter#compensate] end!param:{}", param);
        }
    }

    public void doCompensate(List<BusEventEntity> entityList) {

        if (CollectionUtils.isEmpty(entityList)) {
            return;
        }

        CountDownLatch countDownLatch = new CountDownLatch(entityList.size());
        for (BusEventEntity entity : entityList) {

            // message
            EventMessage eventMessage = new EventMessage();
            eventMessage.setEventId(entity.getEventId());
            eventMessage.setEventData(entity.getEventData());
            eventMessage.setClassName(entity.getClassName());

            compensateExecutor.execute(() -> {
                try {
                    // lock key
                    Pair<String, LockBizType> lockKey = Pair
                            .of(String.valueOf(entity.getEntityId()), LockBizType.EVENT_HANDLE);
                    // consume if try-lock
                    boolean lock = lockSupport.consumeIfTryLock(lockKey, () -> {
                        if (entity.getProcessingState() != EventLifecycleState.AVAILABLE) {
                            asyncEventHandler.handle(eventMessage);
                        } else {
                            Object event = null;
                            try {
                                event = serializer
                                        .deserialize(Class.forName(entity.getClassName()), entity.getEventData());
                            } catch (Exception e) {
                                log.error("[EventCompensateService#compensate]deserialize error!eventId:{}",
                                        entity.getEventId(), e);
                                ExceptionUtils.rethrow(e);
                            }
                            eventSender.retrySend(entity.getEventId(), event);
                        }
                    });
                    log.info("[EventCompensateService#compensate]try-lock!lock:{},eventId:{}", lock,
                            entity.getEventId());
                } catch (Exception ex) {
                    log.error("[EventCompensateService#compensate]doHandle-error!eventMessage:{}", eventMessage, ex);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException ex) {
            log.error("[EventCompensateService#compensate] interrupt-error!", ex);
            Thread.currentThread().interrupt();
            ExceptionUtils.rethrow(ex);
        }
    }
}
