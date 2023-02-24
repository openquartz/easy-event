package org.svnee.easyevent.transfer.kafka.common;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;

/**
 * @author svnee
 **/
@Slf4j
public class KafkaTransferConsumer extends Thread {

    private final KafkaConsumer<String, String> consumer;
    private final CountDownLatch latch;
    private final String topic;
    private final String consumerGroup;
    private final Consumer<ConsumerRecord<String, String>> handler;

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
            log.error("[KafkaTransferConsumer#run]Unexpected termination, exception thrown:" + e);
        } finally {
            shutdown();
        }
    }

    public void doWork() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                ConsumerRecords<String, String> consumerRecords = consumer.poll(Long.MAX_VALUE);
                for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                    handler.accept(consumerRecord);
                }
                consumer.commitSync();
            }
        } catch (WakeupException e) {
            log.error("[KafkaTransferConsumer#doWork] error,topic:{},group:{}", topic, consumerGroup, e);
        } finally {
            consumer.close();
        }
    }

    public void shutdown() {
        try {
            consumer.close();
        } catch (Exception ex) {
            log.error("[KafkaTransferConsumer#shutdown] close consumer error!topic:{},group:{}", topic, consumerGroup,
                ex);
        } finally {
            latch.countDown();
        }
    }
}
