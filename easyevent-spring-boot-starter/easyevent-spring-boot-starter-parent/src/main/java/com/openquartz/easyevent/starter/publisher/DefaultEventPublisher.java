package com.openquartz.easyevent.starter.publisher;

import static com.openquartz.easyevent.common.utils.ParamUtils.checkNotNull;

import com.openquartz.easyevent.core.EventBus;
import com.openquartz.easyevent.core.publisher.EventPublisherAdapter;
import com.openquartz.easyevent.transfer.api.EventSender;

/**
 * 默认事件发布器
 *
 * @author svnee
 **/
public class DefaultEventPublisher extends EventPublisherAdapter {

    private final EventBus directEventBus;
    private final EventSender eventSender;

    public DefaultEventPublisher(EventBus directEventBus, EventSender eventSender) {

        checkNotNull(directEventBus);
        checkNotNull(eventSender);

        this.directEventBus = directEventBus;
        this.eventSender = eventSender;
    }

    @Override
    public EventBus getDirectEventBus() {
        return directEventBus;
    }

    @Override
    public EventSender getEventSender() {
        return eventSender;
    }
}
