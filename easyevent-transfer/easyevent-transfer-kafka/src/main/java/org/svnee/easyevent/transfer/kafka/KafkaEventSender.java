package org.svnee.easyevent.transfer.kafka;

import static org.svnee.easyevent.common.utils.ParamUtils.checkNotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.svnee.easyevent.common.transaction.TransactionSupport;
import org.svnee.easyevent.storage.api.EventStorageService;
import org.svnee.easyevent.transfer.api.adapter.AbstractSenderAdapter;
import org.svnee.easyevent.transfer.api.adapter.TransferProducer;
import org.svnee.easyevent.transfer.api.limiting.EventTransferSenderLimitingControl;
import org.svnee.easyevent.transfer.kafka.common.KafkaTransferProducer;

/**
 * Kafka Sender
 *
 * @author svnee
 **/
@Slf4j
public class KafkaEventSender extends AbstractSenderAdapter {

    private final KafkaTransferProducer kafkaTransferProducer;
    private final EventStorageService eventStorageService;
    private final ExecutorService asyncSendExecutor;
    private final TransactionSupport transactionSupport;
    private final EventTransferSenderLimitingControl eventTransferSenderLimitingControl;

    public KafkaEventSender(KafkaTransferProducer kafkaTransferProducer,
        EventStorageService eventStorageService,
        ExecutorService asyncSendExecutor,
        TransactionSupport transactionSupport,
        EventTransferSenderLimitingControl eventTransferSenderLimitingControl) {

        checkNotNull(kafkaTransferProducer);
        checkNotNull(eventStorageService);
        checkNotNull(asyncSendExecutor);
        checkNotNull(transactionSupport);
        checkNotNull(eventTransferSenderLimitingControl);

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
}
