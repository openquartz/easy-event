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
import org.svnee.easyevent.transfer.kafka.common.KafkaProducer;

/**
 * Kafka Sender
 *
 * @author svnee
 **/
@Slf4j
public class KafkaEventSender extends AbstractSenderAdapter {

    private final KafkaProducer kafkaProducer;
    private final EventStorageService eventStorageService;
    private final ExecutorService asyncSendExecutor;
    private final TransactionSupport transactionSupport;
    private final EventTransferSenderLimitingControl eventTransferSenderLimitingControl;

    public KafkaEventSender(KafkaProducer kafkaProducer,
        EventStorageService eventStorageService,
        ExecutorService asyncSendExecutor,
        TransactionSupport transactionSupport,
        EventTransferSenderLimitingControl eventTransferSenderLimitingControl) {

        checkNotNull(kafkaProducer);
        checkNotNull(eventStorageService);
        checkNotNull(asyncSendExecutor);
        checkNotNull(transactionSupport);
        checkNotNull(eventTransferSenderLimitingControl);

        this.kafkaProducer = kafkaProducer;
        this.eventStorageService = eventStorageService;
        this.asyncSendExecutor = asyncSendExecutor;
        this.transactionSupport = transactionSupport;
        this.eventTransferSenderLimitingControl = eventTransferSenderLimitingControl;
    }

    @Override
    public void init() {
        kafkaProducer.init();
    }

    @Override
    public void destroy() {
        kafkaProducer.destroy();
    }

    @Override
    public TransferProducer getTransferProducer() {
        return kafkaProducer;
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
