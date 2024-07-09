package com.openquartz.easyevent.transfer.rocket;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import com.openquartz.easyevent.transfer.rocket.property.RocketMqCommonProperty;
import lombok.extern.slf4j.Slf4j;
import com.openquartz.easyevent.common.transaction.TransactionSupport;
import com.openquartz.easyevent.storage.api.EventStorageService;
import com.openquartz.easyevent.transfer.api.adapter.AbstractSenderAdapter;
import com.openquartz.easyevent.transfer.api.adapter.TransferProducer;
import com.openquartz.easyevent.transfer.api.limiting.EventTransferSenderLimitingControl;
import com.openquartz.easyevent.transfer.rocket.common.RocketMqProducer;

import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotNull;

/**
 * RocketMQ发送
 *
 * @author svnee
 **/
@Slf4j
public class RocketMqEventSender extends AbstractSenderAdapter {

    private final RocketMqCommonProperty rocketMqCommonProperty;
    private final EventStorageService eventStorageService;
    private final ExecutorService asyncSendExecutor;
    private final TransactionSupport transactionSupport;
    private final RocketMqProducer rocketMqProducer;
    private final EventTransferSenderLimitingControl eventTransferSenderLimitingControl;

    public RocketMqEventSender(
            RocketMqCommonProperty rocketMqCommonProperty,
            EventStorageService eventStorageService,
            ExecutorService asyncSendExecutor,
            TransactionSupport transactionSupport,
            RocketMqProducer rocketMqProducer,
            EventTransferSenderLimitingControl eventTransferSenderLimitingControl) {

        checkNotNull(rocketMqCommonProperty);
        checkNotNull(eventStorageService);
        checkNotNull(asyncSendExecutor);
        checkNotNull(transactionSupport);
        checkNotNull(rocketMqProducer);
        checkNotNull(eventTransferSenderLimitingControl);

        this.rocketMqCommonProperty = rocketMqCommonProperty;
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

    @Override
    public boolean isEnableAsyncSend() {
        return rocketMqCommonProperty.isProduceAsync();
    }
}



