package org.svnee.easyevent.transfer.rocket;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.svnee.easyevent.common.transaction.TransactionSupport;
import org.svnee.easyevent.storage.api.EventStorageService;
import org.svnee.easyevent.transfer.api.adapter.AbstractSenderAdapter;
import org.svnee.easyevent.transfer.api.adapter.TransferProducer;
import org.svnee.easyevent.transfer.api.limiting.EventTransferSenderLimitingControl;
import org.svnee.easyevent.transfer.rocket.common.RocketMqProducer;

/**
 * RocketMQ发送
 *
 * @author svnee
 **/
@Slf4j
public class RocketMqEventSender extends AbstractSenderAdapter {

    private final EventStorageService eventStorageService;
    private final ExecutorService asyncSendExecutor;
    private final TransactionSupport transactionSupport;
    private final RocketMqProducer rocketMqProducer;
    private final EventTransferSenderLimitingControl eventTransferSenderLimitingControl;

    public RocketMqEventSender(EventStorageService eventStorageService,
        ExecutorService asyncSendExecutor,
        TransactionSupport transactionSupport,
        RocketMqProducer rocketMqProducer,
        EventTransferSenderLimitingControl eventTransferSenderLimitingControl) {

        this.eventStorageService = eventStorageService;
        this.asyncSendExecutor = asyncSendExecutor;
        this.transactionSupport = transactionSupport;
        this.rocketMqProducer = rocketMqProducer;
        this.eventTransferSenderLimitingControl = eventTransferSenderLimitingControl;
    }

    @Override
    public void init() {
        rocketMqProducer.init();
    }

    @Override
    public void destroy() {
        rocketMqProducer.destroy();
    }

    @Override
    public TransferProducer getTransferProducer() {
        return rocketMqProducer;
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



