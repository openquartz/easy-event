package com.openquartz.easyevent.starter.trigger;

import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotNull;

import com.openquartz.easyevent.common.property.EasyEventProperties;
import com.openquartz.easyevent.common.serde.Serializer;
import com.openquartz.easyevent.common.transaction.TransactionSupport;
import com.openquartz.easyevent.core.EventBus;
import com.openquartz.easyevent.core.trigger.AsyncEventHandlerAdapter;
import com.openquartz.easyevent.starter.spring.boot.autoconfig.property.EasyEventCommonProperties;
import com.openquartz.easyevent.storage.api.EventStorageService;

/**
 * DefaultEventTrigger
 *
 * @author svnee
 **/
public class DefaultAsyncEventHandler extends AsyncEventHandlerAdapter {

    private final EventBus eventBus;
    private final Serializer serializer;
    private final EventStorageService eventStorageService;
    private final TransactionSupport transactionSupport;
    private final EasyEventCommonProperties eventCommonProperties;

    public DefaultAsyncEventHandler(EventBus eventBus,
        Serializer serializer,
        EventStorageService eventStorageService,
        TransactionSupport transactionSupport,
        EasyEventCommonProperties easyEventCommonProperties) {

        checkNotNull(eventBus);
        checkNotNull(serializer);
        checkNotNull(eventStorageService);
        checkNotNull(transactionSupport);
        checkNotNull(easyEventCommonProperties);

        this.eventBus = eventBus;
        this.serializer = serializer;
        this.eventStorageService = eventStorageService;
        this.transactionSupport = transactionSupport;
        this.eventCommonProperties = easyEventCommonProperties;
    }

    @Override
    public EventBus getHandleEventBus() {
        return eventBus;
    }

    @Override
    public Serializer getSerializer() {
        return serializer;
    }

    @Override
    public EventStorageService getEventStorageService() {
        return eventStorageService;
    }

    @Override
    public TransactionSupport getTransactionSupport() {
        return transactionSupport;
    }

    @Override
    public EasyEventProperties getEasyEventProperties() {
        return eventCommonProperties;
    }
}
