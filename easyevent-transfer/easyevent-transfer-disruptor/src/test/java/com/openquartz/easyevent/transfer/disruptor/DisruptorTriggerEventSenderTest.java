package com.openquartz.easyevent.transfer.disruptor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyevent.common.concurrent.lock.LockSupport;
import com.openquartz.easyevent.common.serde.Serializer;
import com.openquartz.easyevent.common.transaction.AfterTransactionCallback;
import com.openquartz.easyevent.common.transaction.InTransactionCallback;
import com.openquartz.easyevent.common.transaction.TransactionSupport;
import com.openquartz.easyevent.storage.api.EventStorageService;
import com.openquartz.easyevent.storage.identify.EventId;
import com.openquartz.easyevent.storage.model.BaseEventEntity;
import com.openquartz.easyevent.storage.model.EventBody;
import com.openquartz.easyevent.transfer.disruptor.property.DisruptorConsumerProperty;
import com.openquartz.easyevent.transfer.disruptor.property.DisruptorTriggerProperty;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DisruptorTriggerEventSenderTest {

    private DisruptorTriggerEventSender sender;
    private EventStorageService eventStorageService;
    private Serializer serializer;

    @Before
    public void setUp() {
        eventStorageService = mock(EventStorageService.class);
        serializer = mock(Serializer.class);
        when(serializer.serialize(any())).thenReturn("{}");

        TransactionSupport transactionSupport = new TransactionSupport() {
            @Override
            public <T> T execute(InTransactionCallback<T> callback) {
                return callback.doInTransaction();
            }

            @Override
            public void executeAfterCommit(AfterTransactionCallback callback) {
                callback.doAfterCommit();
            }
        };

        DisruptorTriggerProperty property = new DisruptorTriggerProperty();
        DisruptorConsumerProperty consumerProperty = new DisruptorConsumerProperty();
        consumerProperty.setCorePoolSize(1);
        consumerProperty.setMaximumPoolSize(1);
        consumerProperty.setBufferSize(16);
        property.setConsumerProperty(consumerProperty);

        sender = spy(new DisruptorTriggerEventSender(
            property,
            msg -> {
            },
            serializer,
            transactionSupport,
            eventStorageService,
            mock(LockSupport.class)
        ));
    }

    @After
    public void tearDown() {
        if (sender != null) {
            sender.destroy();
        }
    }

    @Test
    public void send_shouldReturnTrueOnSuccess() {
        EventId eventId = new EventId(1L, 1L);
        when(eventStorageService.save(any(EventBody.class))).thenReturn(eventId);

        boolean result = sender.send("hello");

        assertTrue(result);
        verify(eventStorageService).sendComplete(eventId);
    }

    @Test
    public void send_whenPublishFails_shouldMarkSendFailed() {
        EventId eventId = new EventId(2L, 1L);
        when(eventStorageService.save(any(EventBody.class))).thenReturn(eventId);
        doThrow(new RuntimeException("ring-full")).when(sender).publishEvent(any(EventBody.class), any(EventId.class));

        boolean result = sender.send("hello");

        assertTrue(result);
        verify(eventStorageService).sendFailed(any(EventId.class), any(Throwable.class));
    }

    @Test
    public void retrySend_shouldReturnTrueOnSuccess() {
        EventId eventId = new EventId(3L, 1L);
        BaseEventEntity entity = new BaseEventEntity();
        entity.setErrorCount(0);
        when(eventStorageService.getBaseEntity(eventId)).thenReturn(entity);
        when(eventStorageService.isMoreThanMustTrigger(entity)).thenReturn(false);

        EventBody<String> body = new EventBody<>("e", null);
        boolean result = sender.retrySend(eventId, body);

        assertTrue(result);
        verify(eventStorageService).sendComplete(eventId);
    }

    @Test
    public void retrySend_whenOverMax_shouldReturnFalse() {
        EventId eventId = new EventId(4L, 1L);
        BaseEventEntity entity = new BaseEventEntity();
        when(eventStorageService.getBaseEntity(eventId)).thenReturn(entity);
        when(eventStorageService.isMoreThanMustTrigger(entity)).thenReturn(true);

        assertFalse(sender.retrySend(eventId, new EventBody<>("e", null)));
    }
}
