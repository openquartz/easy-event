package com.openquartz.easyevent.transfer.rabbitmq;

import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import lombok.extern.slf4j.Slf4j;
import com.openquartz.easyevent.common.transaction.TransactionSupport;
import com.openquartz.easyevent.storage.api.EventStorageService;
import com.openquartz.easyevent.transfer.api.adapter.AbstractSenderAdapter;
import com.openquartz.easyevent.transfer.api.adapter.TransferProducer;
import com.openquartz.easyevent.transfer.api.limiting.EventTransferSenderLimitingControl;
import com.openquartz.easyevent.transfer.rabbitmq.common.RabbitMqTransferProducer;
import com.openquartz.easyevent.transfer.rabbitmq.property.RabbitMqCommonProperty;

/**
 * RabbitMQ Event Sender
 *
 * @author svnee
 **/
@Slf4j
public class RabbitMqEventSender extends AbstractSenderAdapter {

    private final RabbitMqCommonProperty rabbitMqCommonProperty;
    private final EventStorageService eventStorageService;
    private final ExecutorService asyncSendExecutor;
    private final TransactionSupport transactionSupport;
    private final RabbitMqTransferProducer rabbitMqProducer;
    private final EventTransferSenderLimitingControl eventTransferSenderLimitingControl;

    public RabbitMqEventSender(
        RabbitMqCommonProperty rabbitMqCommonProperty,
        EventStorageService eventStorageService,
        ExecutorService asyncSendExecutor,
        TransactionSupport transactionSupport,
        RabbitMqTransferProducer rabbitMqProducer,
        EventTransferSenderLimitingControl eventTransferSenderLimitingControl) {

        checkNotNull(rabbitMqCommonProperty);
        checkNotNull(eventStorageService);
        checkNotNull(asyncSendExecutor);
        checkNotNull(transactionSupport);
        checkNotNull(rabbitMqProducer);
        checkNotNull(eventTransferSenderLimitingControl);

        this.rabbitMqCommonProperty = rabbitMqCommonProperty;
        this.eventStorageService = eventStorageService;
        this.asyncSendExecutor = asyncSendExecutor;
        this.transactionSupport = transactionSupport;
        this.rabbitMqProducer = rabbitMqProducer;
        this.eventTransferSenderLimitingControl = eventTransferSenderLimitingControl;
    }

    @Override
    public void init() {
        rabbitMqProducer.init();
    }

    @Override
    public void destroy() {
        rabbitMqProducer.destroy();
    }

    @Override
    public TransferProducer getTransferProducer() {
        return rabbitMqProducer;
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
        return rabbitMqCommonProperty.isProduceAsync();
    }
}