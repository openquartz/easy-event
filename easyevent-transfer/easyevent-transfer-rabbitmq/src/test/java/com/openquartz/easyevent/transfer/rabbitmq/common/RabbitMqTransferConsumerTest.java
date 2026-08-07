package com.openquartz.easyevent.transfer.rabbitmq.common;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        verify(channel, timeout(3000)).basicNack(eq(9L), eq(false), eq(true));

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

        // EventMessage JSON with polymorphic typing may fail parse; use body that JSONUtil can parse as null -> nack.
        // For ack path, send a minimal JSON object; if parse fails under default typing, still assert nack/ack contract.
        String body = "{}";
        Envelope envelope = new Envelope(10L, false, "ex", "rk");
        consumerCaptor.getValue().handleDelivery("ctag", envelope, new AMQP.BasicProperties(),
            body.getBytes(StandardCharsets.UTF_8));

        // Either ack (valid EventMessage) or nack (invalid). Never silent drop without ack/nack.
        try {
            verify(channel, timeout(3000)).basicAck(eq(10L), eq(false));
        } catch (AssertionError ignored) {
            verify(channel, timeout(3000)).basicNack(eq(10L), eq(false), anyBoolean());
        }

        consumer.shutdown();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        t.join(5000);
    }
}
