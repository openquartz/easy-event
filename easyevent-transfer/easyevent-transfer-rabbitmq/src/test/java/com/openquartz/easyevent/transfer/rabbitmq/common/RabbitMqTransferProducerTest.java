package com.openquartz.easyevent.transfer.rabbitmq.common;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyevent.common.model.Pair;
import com.openquartz.easyevent.common.serde.Serializer;
import com.openquartz.easyevent.storage.identify.EventId;
import com.openquartz.easyevent.storage.model.EventBody;
import com.openquartz.easyevent.transfer.api.adapter.SendResultCallback;
import com.openquartz.easyevent.transfer.api.route.EventRouter;
import com.openquartz.easyevent.transfer.rabbitmq.property.RabbitMqCommonProperty;
import com.rabbitmq.client.Channel;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class RabbitMqTransferProducerTest {

    private Channel channel;
    private EventRouter eventRouter;
    private RabbitMqTransferProducer producer;

    @Before
    public void setUp() throws Exception {
        channel = mock(Channel.class);
        when(channel.isOpen()).thenReturn(true);
        eventRouter = mock(EventRouter.class);
        when(eventRouter.route(any())).thenReturn(Pair.of("", "q"));

        producer = new RabbitMqTransferProducer(Serializer.DEFAULT, eventRouter, new RabbitMqCommonProperty());
        Field channelField = RabbitMqTransferProducer.class.getDeclaredField("channel");
        channelField.setAccessible(true);
        channelField.set(producer, channel);
    }

    @Test
    public void sendMessage_shouldPublishUnderLock() throws Exception {
        EventBody<String> body = new EventBody<>("x", null);
        EventId eventId = new EventId(1L, 1L);

        producer.sendMessage(body, eventId);

        ArgumentCaptor<byte[]> payloadCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(channel).basicPublish(eq(""), eq("q"), any(), payloadCaptor.capture());
    }

    @Test
    public void asyncSendMessage_shouldInvokeCallbackOnSuccess() throws Exception {
        EventBody<String> body = new EventBody<>("x", null);
        EventId eventId = new EventId(1L, 1L);
        SendResultCallback callback = mock(SendResultCallback.class);

        producer.asyncSendMessage(body, eventId, callback);

        verify(channel).basicPublish(eq(""), eq("q"), any(), any(byte[].class));
        verify(callback).onSuccess(eventId);
    }

    @Test
    public void sendMessageList_shouldPublishForEachEvent() throws Exception {
        EventBody<String> body1 = new EventBody<>("x", null);
        EventBody<String> body2 = new EventBody<>("y", null);
        List<EventBody<String>> bodies = Arrays.asList(body1, body2);
        List<EventId> ids = Arrays.asList(new EventId(1L, 1L), new EventId(2L, 2L));

        producer.sendMessageList(bodies, ids);

        verify(channel, times(2)).basicPublish(eq(""), eq("q"), any(), any(byte[].class));
    }

    @Test
    public void destroy_shouldCloseChannelWhenOpen() throws Exception {
        producer.destroy();
        verify(channel).close();
    }
}
