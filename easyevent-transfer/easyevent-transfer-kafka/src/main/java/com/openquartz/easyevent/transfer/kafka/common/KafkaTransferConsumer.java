package com.openquartz.easyevent.transfer.kafka.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author svnee
 **/
@Slf4j
public class KafkaTransferConsumer extends Thread {

    private static final long POLL_TIMEOUT_MS = 1000L;

    private final KafkaConsumer<String, String> consumer;
    private final CountDownLatch latch;
    private final String topic;
    private final String consumerGroup;
    private final Consumer<ConsumerRecord<String, String>> handler;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public KafkaTransferConsumer(final String topic,
        final String consumerGroup,
        KafkaConsumer<String, String> consumer,
        final CountDownLatch latch,
        Consumer<ConsumerRecord<String, String>> handler) {
        super("KafkaTransferConsumer");

        this.latch = latch;
        this.consumer = consumer;
        this.topic = topic;
        this.consumerGroup = consumerGroup;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            doWork();
        } catch (Exception e) {
            log.error("[KafkaTransferConsumer#run]Unexpected termination, exception thrown:", e);
        } finally {
            closeQuietly();
        }
    }

    private void doWork() {
        try {
            while (running.get() && !Thread.currentThread().isInterrupted()) {
                ConsumerRecords<String, String> consumerRecords = consumer.poll(POLL_TIMEOUT_MS);
                if (consumerRecords.isEmpty()) {
                    continue;
                }

                boolean batchSuccess = true;
                TopicPartition failedPartition = null;
                long failedOffset = -1L;

                for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                    try {
                        handler.accept(consumerRecord);
                    } catch (Exception ex) {
                        batchSuccess = false;
                        failedPartition = new TopicPartition(consumerRecord.topic(), consumerRecord.partition());
                        failedOffset = consumerRecord.offset();
                        log.error(
                            "[KafkaTransferConsumer#doWork] handle record error, topic:{}, group:{}, partition:{}, offset:{}",
                            topic, consumerGroup, consumerRecord.partition(), consumerRecord.offset(), ex);
                        // Stop processing later records in this poll; seek back and retry.
                        break;
                    }
                }

                if (batchSuccess) {
                    try {
                        consumer.commitSync();
                    } catch (WakeupException e) {
                        throw e;
                    } catch (Exception ex) {
                        log.error("[KafkaTransferConsumer#doWork] commit error,topic:{},group:{}", topic, consumerGroup,
                            ex);
                    }
                } else if (failedPartition != null) {
                    try {
                        // Do not advance offsets past a failed record.
                        consumer.seek(failedPartition, failedOffset);
                    } catch (Exception ex) {
                        log.error(
                            "[KafkaTransferConsumer#doWork] seek error,topic:{},group:{},partition:{},offset:{}",
                            topic, consumerGroup, failedPartition.partition(), failedOffset, ex);
                    }
                }
            }
        } catch (WakeupException e) {
            if (running.get()) {
                log.error("[KafkaTransferConsumer#doWork] unexpected wakeup,topic:{},group:{}", topic, consumerGroup, e);
            }
        }
    }

    public void shutdown() {
        running.set(false);
        try {
            consumer.wakeup();
        } catch (Exception ex) {
            log.error("[KafkaTransferConsumer#shutdown] wakeup error!topic:{},group:{}", topic, consumerGroup, ex);
        }
    }

    private void closeQuietly() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        try {
            consumer.close();
        } catch (Exception ex) {
            log.error("[KafkaTransferConsumer#closeQuietly] close consumer error!topic:{},group:{}", topic,
                consumerGroup, ex);
        } finally {
            latch.countDown();
        }
    }
}
