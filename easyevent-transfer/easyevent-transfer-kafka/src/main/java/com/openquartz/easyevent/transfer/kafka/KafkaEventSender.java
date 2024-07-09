package com.openquartz.easyevent.transfer.kafka;

import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import com.openquartz.easyevent.transfer.kafka.property.KafkaCommonProperty;
import lombok.extern.slf4j.Slf4j;
import com.openquartz.easyevent.common.transaction.TransactionSupport;
import com.openquartz.easyevent.storage.api.EventStorageService;
import com.openquartz.easyevent.transfer.api.adapter.AbstractSenderAdapter;
import com.openquartz.easyevent.transfer.api.adapter.TransferProducer;
import com.openquartz.easyevent.transfer.api.limiting.EventTransferSenderLimitingControl;
import com.openquartz.easyevent.transfer.kafka.common.KafkaTransferProducer;

/**
 * Kafka Sender
 *
 * @author svnee
 **/
@Slf4j
public class KafkaEventSender extends AbstractSenderAdapter {

    private final KafkaCommonProperty kafkaCommonProperty;
    private final KafkaTransferProducer kafkaTransferProducer;
    private final EventStorageService eventStorageService;
    private final ExecutorService asyncSendExecutor;
    private final TransactionSupport transactionSupport;
    private final EventTransferSenderLimitingControl eventTransferSenderLimitingControl;

    public KafkaEventSender(
            KafkaCommonProperty kafkaCommonProperty,
            KafkaTransferProducer kafkaTransferProducer,
            EventStorageService eventStorageService,
            ExecutorService asyncSendExecutor,
            TransactionSupport transactionSupport,
            EventTransferSenderLimitingControl eventTransferSenderLimitingControl) {

        checkNotNull(kafkaCommonProperty);
        checkNotNull(kafkaTransferProducer);
        checkNotNull(eventStorageService);
        checkNotNull(asyncSendExecutor);
        checkNotNull(transactionSupport);
        checkNotNull(eventTransferSenderLimitingControl);

        this.kafkaCommonProperty = kafkaCommonProperty;
        this.kafkaTransferProducer = kafkaTransferProducer;
        this.eventStorageService = eventStorageService;
        this.asyncSendExecutor = asyncSendExecutor;
        this.transactionSupport = transactionSupport;
        this.eventTransferSenderLimitingControl = eventTransferSenderLimitingControl;
    }

    @Override
    public void init() {
        kafkaTransferProducer.init();
    }

    @Override
    public void destroy() {
        kafkaTransferProducer.destroy();
    }

    @Override
    public TransferProducer getTransferProducer() {
        return kafkaTransferProducer;
    }

    @Override
    public TransactionSupport getTransactionSupport() {
        return transactionSupport;
    }

    @Override
    public EventStorageService getEventStorageService() {
        return eventStorageService;
    }

    @Override
    public EventTransferSenderLimitingControl getEventTransferSenderLimitingControl() {
        return eventTransferSenderLimitingControl;
    }

    @Override
    public Executor getAsyncSendExecutor() {
        return asyncSendExecutor;
    }

    @Override
    public boolean isEnableAsyncSend() {
        return kafkaCommonProperty.isProduceAsync();
    }
}
