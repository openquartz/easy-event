package com.openquartz.easyevent.storage.jdbc;

import com.openquartz.easyevent.common.concurrent.TraceContext;
import com.openquartz.easyevent.common.constant.CommonConstants;
import com.openquartz.easyevent.common.property.EasyEventProperties;
import com.openquartz.easyevent.common.serde.Serializer;
import com.openquartz.easyevent.common.utils.CollectionUtils;
import com.openquartz.easyevent.common.utils.IpUtil;
import com.openquartz.easyevent.common.utils.StringUtils;
import com.openquartz.easyevent.storage.api.EventStorageService;
import com.openquartz.easyevent.storage.identify.EventId;
import com.openquartz.easyevent.storage.identify.IdGenerator;
import com.openquartz.easyevent.storage.jdbc.mapper.BusEventEntityMapper;
import com.openquartz.easyevent.storage.model.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotEmpty;
import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotNull;

/**
 * @author svnee
 **/
public class JdbcEventStorageServiceImpl implements EventStorageService {

    private final BusEventEntityMapper busEventEntityMapper;
    private final Serializer serializer;
    private final IdGenerator idGenerator;
    private final EasyEventProperties easyEventProperties;

    public JdbcEventStorageServiceImpl(BusEventEntityMapper busEventEntityMapper,
                                       Serializer serializer,
                                       IdGenerator idGenerator,
                                       EasyEventProperties easyEventProperties) {

        checkNotNull(busEventEntityMapper);
        checkNotNull(serializer);
        checkNotNull(easyEventProperties);

        this.busEventEntityMapper = busEventEntityMapper;
        this.idGenerator = idGenerator;
        this.serializer = serializer;
        this.easyEventProperties = easyEventProperties;
    }

    @Override
    public <T> EventId save(EventBody<T> eventBody) {
        BusEventEntity busEventEntity = buildInitBusEventEntity(eventBody);
        if (Objects.nonNull(idGenerator)) {
            long eventEntityId = idGenerator.generateId(eventBody.getEvent());
            busEventEntity.setEntityId(eventEntityId);
        }
        Long sourceId = TraceContext.getSourceEventId();
        busEventEntity.setSourceId(sourceId);
        busEventEntityMapper.insertSelective(busEventEntity);
        return busEventEntity.getEventId();
    }

    private <T> BusEventEntity buildInitBusEventEntity(EventBody<T> eventBody) {

        Date now = new Date();

        String traceId = TraceContext.getTraceId();
        BusEventEntity busEventEntity = new BusEventEntity();
        busEventEntity.setTraceId(StringUtils.isNotBlank(traceId) ? traceId : CommonConstants.EMPTY_STRING);
        busEventEntity.setEventData(serializer.serialize(eventBody));
        busEventEntity.setEventKey(eventBody.computeEventKey());
        busEventEntity.setCreatingOwner(IpUtil.getIp());
        busEventEntity.setProcessingOwner(CommonConstants.EMPTY_STRING);
        busEventEntity.setProcessingAvailableDate(null);
        busEventEntity.setProcessingFailedReason(CommonConstants.EMPTY_STRING);
        busEventEntity.setCreatedTime(now);
        busEventEntity.setUpdatedTime(now);
        busEventEntity.setClassName(eventBody.getEvent().getClass().getName());
        busEventEntity.setErrorCount(0);
        busEventEntity.setSuccessfulSubscriberList(Collections.emptyList());
        busEventEntity.setProcessingState(EventLifecycleState.AVAILABLE);
        busEventEntity.setAppId(easyEventProperties.getAppId());
        return busEventEntity;
    }

    @Override
    public <T> List<EventId> saveList(List<EventBody<T>> eventList) {
        if (CollectionUtils.isEmpty(eventList)) {
            return Collections.emptyList();
        }
        Long sourceId = TraceContext.getSourceEventId();

        List<BusEventEntity> entityList = eventList.stream()
                .map(this::buildInitBusEventEntity)
                .peek(k -> k.setSourceId(sourceId))
                .collect(Collectors.toList());

        if (Objects.nonNull(idGenerator)) {
            for (int i = 0; i < entityList.size(); i++) {
                BusEventEntity busEventEntity = entityList.get(i);
                busEventEntity.setEntityId(idGenerator.generateId(eventList.get(i).getEvent()));
            }
            busEventEntityMapper.insertListWithSupplierId(entityList);
        } else {
            busEventEntityMapper.insertList(entityList);
        }
        return entityList.stream().map(BaseEventEntity::getEventId).collect(Collectors.toList());
    }

    @Override
    public void sendComplete(EventId eventId) {
        busEventEntityMapper.refreshSendComplete(eventId, EventLifecycleState.TRANSFER_SUCCESS);
    }

    @Override
    public void sendCompleteList(List<EventId> eventIdList) {

        checkNotEmpty(eventIdList);

        busEventEntityMapper.batchRefreshSendComplete(eventIdList, EventLifecycleState.TRANSFER_SUCCESS);
    }

    @Override
    public void sendFailed(EventId eventId, Throwable ex) {
        busEventEntityMapper.refreshSendFailed(eventId, EventLifecycleState.TRANSFER_FAILED, ex);
    }

    @Override
    public void sendFailedList(List<EventId> eventIdList, Throwable ex) {

        checkNotEmpty(eventIdList);

        eventIdList.stream()
                .sorted((Comparator.comparing(EventId::getId)))
                .forEach(k -> sendFailed(k, ex));
    }

    @Override
    public BaseEventEntity getBaseEntity(EventId eventId) {
        return busEventEntityMapper.getBaseEntity(eventId);
    }

    @Override
    public void processingFailed(EventId eventId, Throwable ex) {
        processingFailed(eventId, Collections.emptyList(), ex);
    }

    @Override
    public void processingFailed(EventId eventId, List<String> successSubscriberList, Throwable invokeError) {

        List<String> subscriberIdentifyList = busEventEntityMapper.getSuccessfulSubscriberIdentify(eventId);
        List<String> successfulSubscriberList = CollectionUtils
                .mergeNoRepeat(subscriberIdentifyList, successSubscriberList);

        busEventEntityMapper
                .processingFailed(eventId, EventLifecycleState.PROCESS_FAILED, successfulSubscriberList, invokeError);
    }

    @Override
    public void processingCompleted(EventId eventId) {
        busEventEntityMapper.processingComplete(eventId, EventLifecycleState.PROCESS_COMPLETE);
    }

    @Override
    public boolean isMoreThanMustTrigger(BaseEventEntity eventEntity) {
        return eventEntity.getErrorCount() > easyEventProperties.getMaxRetryCount();
    }

    @Override
    public boolean startProcessing(EventId eventId) {
        busEventEntityMapper.refreshStartProcessing(eventId, EventLifecycleState.IN_PROCESSING);
        return true;
    }

    @Override
    public List<BusEventEntity> get(BusEventSelectorCondition condition) {
        return busEventEntityMapper.getBySelectiveCondition(condition);
    }
}
