package com.openquartz.easyevent.core.trigger;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyevent.common.concurrent.TraceContext;
import com.openquartz.easyevent.common.property.EasyEventProperties;
import com.openquartz.easyevent.common.serde.Serializer;
import com.openquartz.easyevent.common.transaction.AfterTransactionCallback;
import com.openquartz.easyevent.common.transaction.InTransactionCallback;
import com.openquartz.easyevent.common.transaction.TransactionSupport;
import com.openquartz.easyevent.core.EventBus;
import com.openquartz.easyevent.core.dispatcher.DispatchInvokeResult;
import com.openquartz.easyevent.storage.api.EventStorageService;
import com.openquartz.easyevent.storage.identify.EventId;
import com.openquartz.easyevent.storage.model.BaseEventEntity;
import com.openquartz.easyevent.storage.model.EventBody;
import com.openquartz.easyevent.storage.model.EventLifecycleState;
import com.openquartz.easyevent.transfer.api.message.EventMessage;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AsyncEventHandlerAdapterTest {

    private EventBus eventBus;
    private Serializer serializer;
    private EventStorageService eventStorageService;
    private EasyEventProperties easyEventProperties;
    private AtomicInteger txExecutions;
    private AsyncEventHandlerAdapter handler;

    @Before
    public void setUp() {
        eventBus = mock(EventBus.class);
        serializer = mock(Serializer.class);
        eventStorageService = mock(EventStorageService.class);
        easyEventProperties = mock(EasyEventProperties.class);
        when(easyEventProperties.getMaxRetryCount()).thenReturn(10);
        txExecutions = new AtomicInteger();

        TransactionSupport transactionSupport = new TransactionSupport() {
            @Override
            public <T> T execute(InTransactionCallback<T> callback) {
                txExecutions.incrementAndGet();
                return callback.doInTransaction();
            }

            @Override
            public void executeAfterCommit(AfterTransactionCallback callback) {
                callback.doAfterCommit();
            }
        };

        handler = new AsyncEventHandlerAdapter() {
            @Override
            public EventBus getHandleEventBus() {
                return eventBus;
            }

            @Override
            public Serializer getSerializer() {
                return serializer;
            }

            @Override
            public EventStorageService getEventStorageService() {
                return eventStorageService;
            }

            @Override
            public TransactionSupport getTransactionSupport() {
                return transactionSupport;
            }

            @Override
            public EasyEventProperties getEasyEventProperties() {
                return easyEventProperties;
            }
        };
    }

    @After
    public void tearDown() {
        TraceContext.clear();
    }

    @Test
    public void handle_whenEntityMissing_shouldSkipWithoutNpe() {
        EventId eventId = new EventId(1L, 1L);
        EventMessage message = new EventMessage();
        message.setEventId(eventId);
        when(eventStorageService.getBaseEntity(eventId)).thenReturn(null);

        handler.handle(message);

        verify(eventStorageService, never()).startProcessing(any());
    }

    @Test
    public void handle_shouldClearTraceContextInFinally() {
        EventId eventId = new EventId(2L, 1L);
        EventMessage message = new EventMessage();
        message.setEventId(eventId);
        message.setEventData("{}");

        BaseEventEntity entity = new BaseEventEntity();
        entity.setEntityId(2L);
        entity.setErrorCount(0);
        entity.setProcessingState(EventLifecycleState.TRANSFER_SUCCESS);
        entity.setTraceId("trace-xyz");
        entity.setSuccessfulSubscriberList(Collections.emptyList());
        when(eventStorageService.getBaseEntity(eventId)).thenReturn(entity);
        when(eventStorageService.startProcessing(eventId)).thenReturn(true);

        EventBody<Object> body = new EventBody<>();
        body.setEvent(new Object());
        body.setContext(null);
        when(serializer.deserialize(eq(EventBody.class), any())).thenReturn(body);

        DispatchInvokeResult ok = new DispatchInvokeResult(body.getEvent());
        when(eventBus.postAll(any(), anyList(), anyBoolean())).thenReturn(ok);

        handler.handle(message);

        assertNull(TraceContext.getTraceId());
        assertNull(TraceContext.getSourceEventId());
    }

    @Test
    public void handle_whenJoinSubscriberFails_shouldRollbackAndPersistFailureInSeparateTx() {
        EventId eventId = new EventId(3L, 1L);
        EventMessage message = new EventMessage();
        message.setEventId(eventId);
        message.setEventData("{}");

        BaseEventEntity entity = new BaseEventEntity();
        entity.setEntityId(3L);
        entity.setErrorCount(0);
        entity.setProcessingState(EventLifecycleState.TRANSFER_SUCCESS);
        entity.setSuccessfulSubscriberList(Collections.emptyList());
        when(eventStorageService.getBaseEntity(eventId)).thenReturn(entity);
        when(eventStorageService.startProcessing(eventId)).thenReturn(true);

        Object event = new Object();
        EventBody<Object> body = new EventBody<>();
        body.setEvent(event);
        when(serializer.deserialize(eq(EventBody.class), any())).thenReturn(body);

        DispatchInvokeResult noJoinOk = new DispatchInvokeResult(event);
        DispatchInvokeResult joinFailed = new DispatchInvokeResult(event);
        joinFailed.setInvokeError(new RuntimeException("subscriber-failed"));
        when(eventBus.postAll(eq(event), anyList(), eq(false))).thenReturn(noJoinOk);
        when(eventBus.postAll(eq(event), anyList(), eq(true))).thenReturn(joinFailed);

        try {
            handler.handle(message);
            fail("expected handle failure to be rethrown");
        } catch (RuntimeException ignored) {
            // expected
        }

        // first TX throws (rollback), second TX records processingFailed
        assertTrue(txExecutions.get() >= 2);
        verify(eventStorageService, times(1)).processingFailed(eq(eventId), anyList(), any(Throwable.class));
        verify(eventStorageService, never()).processingCompleted(eventId);
    }

    @Test
    public void handle_whenStartProcessingReturnsFalse_shouldSkip() {
        EventId eventId = new EventId(4L, 1L);
        EventMessage message = new EventMessage();
        message.setEventId(eventId);
        message.setEventData("{}");

        BaseEventEntity entity = new BaseEventEntity();
        entity.setEntityId(4L);
        entity.setErrorCount(0);
        entity.setProcessingState(EventLifecycleState.IN_PROCESSING);
        entity.setSuccessfulSubscriberList(Collections.emptyList());
        when(eventStorageService.getBaseEntity(eventId)).thenReturn(entity);
        when(eventStorageService.startProcessing(eventId)).thenReturn(false);
        when(serializer.deserialize(eq(EventBody.class), any())).thenReturn(new EventBody<>());

        handler.handle(message);

        verify(eventBus, never()).postAll(any(), anyList(), anyBoolean());
    }
}
