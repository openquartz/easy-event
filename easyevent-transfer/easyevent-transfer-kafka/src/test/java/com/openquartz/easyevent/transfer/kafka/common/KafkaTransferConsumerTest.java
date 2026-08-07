package com.openquartz.easyevent.transfer.kafka.common;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.junit.Test;

public class KafkaTransferConsumerTest {

    @Test
    public void handlerException_shouldNotKillConsumerLoop() throws Exception {
        @SuppressWarnings("unchecked")
        KafkaConsumer<String, String> kafkaConsumer = mock(KafkaConsumer.class);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger handled = new AtomicInteger();

        TopicPartition tp = new TopicPartition("topic", 0);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0L, "k", "v");
        ConsumerRecords<String, String> records =
            new ConsumerRecords<>(Collections.singletonMap(tp, Collections.singletonList(record)));

        AtomicInteger polls = new AtomicInteger();
        when(kafkaConsumer.poll(anyLong())).thenAnswer(invocation -> {
            if (polls.getAndIncrement() == 0) {
                return records;
            }
            throw new WakeupException();
        });

        KafkaTransferConsumer consumer = new KafkaTransferConsumer("topic", "group", kafkaConsumer, latch, r -> {
            handled.incrementAndGet();
            throw new RuntimeException("poison");
        });

        Thread t = new Thread(consumer);
        t.start();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        t.join(5000);

        assertTrue(handled.get() >= 1);
        verify(kafkaConsumer, atLeastOnce()).commitSync();
        verify(kafkaConsumer, atLeastOnce()).close();
    }

    @Test
    public void shutdown_shouldWakeupConsumer() {
        @SuppressWarnings("unchecked")
        KafkaConsumer<String, String> kafkaConsumer = mock(KafkaConsumer.class);
        CountDownLatch latch = new CountDownLatch(1);
        KafkaTransferConsumer consumer = new KafkaTransferConsumer("topic", "group", kafkaConsumer, latch, r -> {
        });

        doAnswer(invocation -> null).when(kafkaConsumer).wakeup();
        consumer.shutdown();
        verify(kafkaConsumer).wakeup();
    }
}
