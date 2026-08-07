package com.openquartz.easyevent.transfer.kafka.common;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
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
    public void handlerException_shouldSeekAndNotCommit() throws Exception {
        @SuppressWarnings("unchecked")
        KafkaConsumer<String, String> kafkaConsumer = mock(KafkaConsumer.class);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger handled = new AtomicInteger();

        TopicPartition tp = new TopicPartition("topic", 0);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 7L, "k", "v");
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
        verify(kafkaConsumer, never()).commitSync();
        verify(kafkaConsumer).seek(eq(tp), eq(7L));
        verify(kafkaConsumer, atLeastOnce()).close();
    }

    @Test
    public void handlerSuccess_shouldCommit() throws Exception {
        @SuppressWarnings("unchecked")
        KafkaConsumer<String, String> kafkaConsumer = mock(KafkaConsumer.class);
        CountDownLatch latch = new CountDownLatch(1);

        TopicPartition tp = new TopicPartition("topic", 0);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 1L, "k", "v");
        ConsumerRecords<String, String> records =
            new ConsumerRecords<>(Collections.singletonMap(tp, Collections.singletonList(record)));

        AtomicInteger polls = new AtomicInteger();
        when(kafkaConsumer.poll(anyLong())).thenAnswer(invocation -> {
            if (polls.getAndIncrement() == 0) {
                return records;
            }
            throw new WakeupException();
        });

        KafkaTransferConsumer consumer =
            new KafkaTransferConsumer("topic", "group", kafkaConsumer, latch, r -> {
            });

        Thread t = new Thread(consumer);
        t.start();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        t.join(5000);

        verify(kafkaConsumer, atLeastOnce()).commitSync();
        verify(kafkaConsumer, never()).seek(any(TopicPartition.class), anyLong());
    }

    @Test
    public void handlerException_shouldStopLaterRecordsInBatch() throws Exception {
        @SuppressWarnings("unchecked")
        KafkaConsumer<String, String> kafkaConsumer = mock(KafkaConsumer.class);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger handled = new AtomicInteger();

        TopicPartition tp = new TopicPartition("topic", 0);
        ConsumerRecord<String, String> r1 = new ConsumerRecord<>("topic", 0, 1L, "k1", "v1");
        ConsumerRecord<String, String> r2 = new ConsumerRecord<>("topic", 0, 2L, "k2", "v2");
        ConsumerRecords<String, String> records =
            new ConsumerRecords<>(Collections.singletonMap(tp, Arrays.asList(r1, r2)));

        AtomicInteger polls = new AtomicInteger();
        when(kafkaConsumer.poll(anyLong())).thenAnswer(invocation -> {
            if (polls.getAndIncrement() == 0) {
                return records;
            }
            throw new WakeupException();
        });

        KafkaTransferConsumer consumer = new KafkaTransferConsumer("topic", "group", kafkaConsumer, latch, r -> {
            handled.incrementAndGet();
            throw new RuntimeException("fail-first");
        });

        Thread t = new Thread(consumer);
        t.start();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        t.join(5000);

        assertTrue(handled.get() == 1);
        verify(kafkaConsumer).seek(eq(tp), eq(1L));
        verify(kafkaConsumer, never()).commitSync();
    }

    @Test
    public void poll_shouldUse1000MsTimeout() throws Exception {
        @SuppressWarnings("unchecked")
        KafkaConsumer<String, String> kafkaConsumer = mock(KafkaConsumer.class);
        CountDownLatch latch = new CountDownLatch(1);

        when(kafkaConsumer.poll(eq(1000L))).thenAnswer(invocation -> {
            throw new WakeupException();
        });

        KafkaTransferConsumer consumer =
            new KafkaTransferConsumer("topic", "group", kafkaConsumer, latch, r -> {
            });

        Thread t = new Thread(consumer);
        t.start();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        t.join(5000);

        verify(kafkaConsumer, atLeastOnce()).poll(eq(1000L));
    }

    @Test
    public void emptyPoll_shouldNotCommit() throws Exception {
        @SuppressWarnings("unchecked")
        KafkaConsumer<String, String> kafkaConsumer = mock(KafkaConsumer.class);
        CountDownLatch latch = new CountDownLatch(1);

        AtomicInteger polls = new AtomicInteger();
        when(kafkaConsumer.poll(eq(1000L))).thenAnswer(invocation -> {
            if (polls.getAndIncrement() == 0) {
                return new ConsumerRecords<>(Collections.emptyMap());
            }
            throw new WakeupException();
        });

        KafkaTransferConsumer consumer =
            new KafkaTransferConsumer("topic", "group", kafkaConsumer, latch, r -> {
            });

        Thread t = new Thread(consumer);
        t.start();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        t.join(5000);

        verify(kafkaConsumer, never()).commitSync();
    }

    @Test
    public void partialBatch_firstOkSecondFails_shouldSeekSecondOffsetAndNotCommit() throws Exception {
        @SuppressWarnings("unchecked")
        KafkaConsumer<String, String> kafkaConsumer = mock(KafkaConsumer.class);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger handled = new AtomicInteger();

        TopicPartition tp = new TopicPartition("topic", 0);
        ConsumerRecord<String, String> r1 = new ConsumerRecord<>("topic", 0, 1L, "k1", "v1");
        ConsumerRecord<String, String> r2 = new ConsumerRecord<>("topic", 0, 2L, "k2", "v2");
        ConsumerRecords<String, String> records =
            new ConsumerRecords<>(Collections.singletonMap(tp, Arrays.asList(r1, r2)));

        AtomicInteger polls = new AtomicInteger();
        when(kafkaConsumer.poll(anyLong())).thenAnswer(invocation -> {
            if (polls.getAndIncrement() == 0) {
                return records;
            }
            throw new WakeupException();
        });

        KafkaTransferConsumer consumer = new KafkaTransferConsumer("topic", "group", kafkaConsumer, latch, r -> {
            handled.incrementAndGet();
            if (r.offset() == 2L) {
                throw new RuntimeException("fail-second");
            }
        });

        Thread t = new Thread(consumer);
        t.start();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        t.join(5000);

        assertTrue(handled.get() == 2);
        verify(kafkaConsumer).seek(eq(tp), eq(2L));
        verify(kafkaConsumer, never()).commitSync();
    }

    @Test
    public void commitWakeupException_shouldExitLoopAndCloseOnce() throws Exception {
        @SuppressWarnings("unchecked")
        KafkaConsumer<String, String> kafkaConsumer = mock(KafkaConsumer.class);
        CountDownLatch latch = new CountDownLatch(1);

        TopicPartition tp = new TopicPartition("topic", 0);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 3L, "k", "v");
        ConsumerRecords<String, String> records =
            new ConsumerRecords<>(Collections.singletonMap(tp, Collections.singletonList(record)));

        AtomicInteger polls = new AtomicInteger();
        when(kafkaConsumer.poll(anyLong())).thenAnswer(invocation -> {
            if (polls.getAndIncrement() == 0) {
                return records;
            }
            throw new WakeupException();
        });
        doThrow(new WakeupException()).when(kafkaConsumer).commitSync();

        KafkaTransferConsumer consumer =
            new KafkaTransferConsumer("topic", "group", kafkaConsumer, latch, r -> {
            });

        Thread t = new Thread(consumer);
        t.start();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        t.join(5000);

        verify(kafkaConsumer, times(1)).close();
    }

    @Test
    public void seekException_shouldStillClose() throws Exception {
        @SuppressWarnings("unchecked")
        KafkaConsumer<String, String> kafkaConsumer = mock(KafkaConsumer.class);
        CountDownLatch latch = new CountDownLatch(1);

        TopicPartition tp = new TopicPartition("topic", 0);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 4L, "k", "v");
        ConsumerRecords<String, String> records =
            new ConsumerRecords<>(Collections.singletonMap(tp, Collections.singletonList(record)));

        AtomicInteger polls = new AtomicInteger();
        when(kafkaConsumer.poll(anyLong())).thenAnswer(invocation -> {
            if (polls.getAndIncrement() == 0) {
                return records;
            }
            throw new WakeupException();
        });
        doThrow(new RuntimeException("seek-fail")).when(kafkaConsumer).seek(any(TopicPartition.class), anyLong());

        KafkaTransferConsumer consumer = new KafkaTransferConsumer("topic", "group", kafkaConsumer, latch, r -> {
            throw new RuntimeException("handler-fail");
        });

        Thread t = new Thread(consumer);
        t.start();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        t.join(5000);

        verify(kafkaConsumer, times(1)).close();
    }

    @Test
    public void closeQuietly_shouldCloseConsumerOnlyOnce() throws Exception {
        @SuppressWarnings("unchecked")
        KafkaConsumer<String, String> kafkaConsumer = mock(KafkaConsumer.class);
        CountDownLatch latch = new CountDownLatch(1);

        TopicPartition tp = new TopicPartition("topic", 0);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 5L, "k", "v");
        ConsumerRecords<String, String> records =
            new ConsumerRecords<>(Collections.singletonMap(tp, Collections.singletonList(record)));

        AtomicInteger polls = new AtomicInteger();
        when(kafkaConsumer.poll(anyLong())).thenAnswer(invocation -> {
            int n = polls.getAndIncrement();
            if (n == 0) {
                return records;
            }
            throw new WakeupException();
        });

        KafkaTransferConsumer consumer = new KafkaTransferConsumer("topic", "group", kafkaConsumer, latch, r -> {
            throw new RuntimeException("handler-fail");
        });

        Thread t = new Thread(consumer);
        t.start();
        Thread.sleep(200);
        consumer.shutdown();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        t.join(5000);

        verify(kafkaConsumer, times(1)).close();
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
