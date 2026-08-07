package com.openquartz.easyevent.transfer.rabbitmq.common;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyevent.common.utils.JSONUtil;
import com.openquartz.easyevent.storage.identify.EventId;
import com.openquartz.easyevent.transfer.api.message.EventMessage;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class RabbitMqTransferConsumerTest {

    @Test
    public void blankPayload_shouldNackNotAck() throws Exception {
        Channel channel = mock(Channel.class);
        Connection connection = mock(Connection.class);
        when(channel.isOpen()).thenReturn(true);
        when(connection.isOpen()).thenReturn(true);
        when(channel.basicConsume(anyString(), eq(false), any(Consumer.class))).thenReturn("ctag");

        CountDownLatch latch = new CountDownLatch(1);
        RabbitMqTransferConsumer consumer =
            new RabbitMqTransferConsumer(channel, connection, "q", "g", latch, msg -> {
            });

        Thread t = new Thread(consumer);
        t.start();

        ArgumentCaptor<Consumer> consumerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(channel, timeout(3000)).basicConsume(eq("q"), eq(false), consumerCaptor.capture());

        Envelope envelope = new Envelope(9L, false, "ex", "rk");
        consumerCaptor.getValue().handleDelivery("ctag", envelope, new AMQP.BasicProperties(),
            "".getBytes(StandardCharsets.UTF_8));

        verify(channel, timeout(3000)).basicNack(eq(9L), eq(false), eq(false));

        consumer.shutdown();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        t.join(5000);
    }

    @Test
    public void handlerThrows_shouldNackWithRequeueFalse() throws Exception {
        Channel channel = mock(Channel.class);
        Connection connection = mock(Connection.class);
        when(channel.isOpen()).thenReturn(true);
        when(connection.isOpen()).thenReturn(true);
        when(channel.basicConsume(anyString(), eq(false), any(Consumer.class))).thenReturn("ctag");

        CountDownLatch latch = new CountDownLatch(1);
        RabbitMqTransferConsumer consumer =
            new RabbitMqTransferConsumer(channel, connection, "q", "g", latch, msg -> {
                throw new RuntimeException("handler-fail");
            });

        Thread t = new Thread(consumer);
        t.start();

        ArgumentCaptor<Consumer> consumerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(channel, timeout(3000)).basicConsume(eq("q"), eq(false), consumerCaptor.capture());

        String body = buildValidEventMessageBody();
        Envelope envelope = new Envelope(11L, false, "ex", "rk");
        consumerCaptor.getValue().handleDelivery("ctag", envelope, new AMQP.BasicProperties(),
            body.getBytes(StandardCharsets.UTF_8));

        verify(channel, timeout(3000)).basicNack(eq(11L), eq(false), eq(false));
        verify(channel, never()).basicAck(eq(11L), eq(false));

        consumer.shutdown();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        t.join(5000);
    }

    @Test
    public void validEventMessage_shouldAckExactlyOnce() throws Exception {
        Channel channel = mock(Channel.class);
        Connection connection = mock(Connection.class);
        when(channel.isOpen()).thenReturn(true);
        when(connection.isOpen()).thenReturn(true);
        when(channel.basicConsume(anyString(), eq(false), any(Consumer.class))).thenReturn("ctag");

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Boolean> handled = new AtomicReference<>(false);
        RabbitMqTransferConsumer consumer =
            new RabbitMqTransferConsumer(channel, connection, "q", "g", latch, msg -> handled.set(true));

        Thread t = new Thread(consumer);
        t.start();

        ArgumentCaptor<Consumer> consumerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(channel, timeout(3000)).basicConsume(eq("q"), eq(false), consumerCaptor.capture());

        String body = buildValidEventMessageBody();
        Envelope envelope = new Envelope(12L, false, "ex", "rk");
        consumerCaptor.getValue().handleDelivery("ctag", envelope, new AMQP.BasicProperties(),
            body.getBytes(StandardCharsets.UTF_8));

        verify(channel, timeout(3000)).basicAck(eq(12L), eq(false));
        verify(channel, never()).basicNack(eq(12L), eq(false), anyBoolean());
        assertTrue(handled.get());

        consumer.shutdown();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        t.join(5000);
    }

    @Test
    public void nackFailure_shouldNotKillThread() throws Exception {
        Channel channel = mock(Channel.class);
        Connection connection = mock(Connection.class);
        when(channel.isOpen()).thenReturn(true);
        when(connection.isOpen()).thenReturn(true);
        when(channel.basicConsume(anyString(), eq(false), any(Consumer.class))).thenReturn("ctag");
        doThrow(new RuntimeException("nack-fail")).when(channel).basicNack(eq(13L), eq(false), eq(false));

        CountDownLatch latch = new CountDownLatch(1);
        RabbitMqTransferConsumer consumer =
            new RabbitMqTransferConsumer(channel, connection, "q", "g", latch, msg -> {
            });

        Thread t = new Thread(consumer);
        t.start();

        ArgumentCaptor<Consumer> consumerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(channel, timeout(3000)).basicConsume(eq("q"), eq(false), consumerCaptor.capture());

        Envelope envelope = new Envelope(13L, false, "ex", "rk");
        consumerCaptor.getValue().handleDelivery("ctag", envelope, new AMQP.BasicProperties(),
            "".getBytes(StandardCharsets.UTF_8));

        consumer.shutdown();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        t.join(5000);
    }

    @Test
    public void validPayload_shouldAck() throws Exception {
        Channel channel = mock(Channel.class);
        Connection connection = mock(Connection.class);
        when(channel.isOpen()).thenReturn(true);
        when(connection.isOpen()).thenReturn(true);
        when(channel.basicConsume(anyString(), eq(false), any(Consumer.class))).thenReturn("ctag");

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Boolean> handled = new AtomicReference<>(false);
        RabbitMqTransferConsumer consumer =
            new RabbitMqTransferConsumer(channel, connection, "q", "g", latch, msg -> handled.set(true));

        Thread t = new Thread(consumer);
        t.start();

        ArgumentCaptor<Consumer> consumerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(channel, timeout(3000)).basicConsume(eq("q"), eq(false), consumerCaptor.capture());

        String body = buildValidEventMessageBody();
        Envelope envelope = new Envelope(10L, false, "ex", "rk");
        consumerCaptor.getValue().handleDelivery("ctag", envelope, new AMQP.BasicProperties(),
            body.getBytes(StandardCharsets.UTF_8));

        verify(channel, timeout(3000)).basicAck(eq(10L), eq(false));

        consumer.shutdown();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        t.join(5000);
    }

    private static String buildValidEventMessageBody() {
        EventMessage msg = new EventMessage();
        msg.setEventId(new EventId(1L, 1L));
        msg.setClassName("java.lang.String");
        msg.setEventData("\"hi\"");
        return JSONUtil.toJson(msg);
    }
}
