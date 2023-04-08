package com.openquartz.easyevent.transfer.api.message;

import com.openquartz.easyevent.common.serde.Serializer;
import com.openquartz.easyevent.storage.identify.EventId;

/**
 * @author svnee
 **/
public class EventMessageBuilder {

    private Object event;
    private EventId eventId;
    private Serializer serializer;

    private EventMessageBuilder() {
    }

    public static EventMessageBuilder builder() {
        return new EventMessageBuilder();
    }

    public EventMessageBuilder event(Object event) {
        this.event = event;
        return this;
    }

    public EventMessageBuilder eventId(EventId eventId) {
        this.eventId = eventId;
        return this;
    }

    public EventMessageBuilder serializer(Serializer serializer) {
        this.serializer = serializer;
        return this;
    }

    public EventMessage build() {
        EventMessage eventMessage = new EventMessage();
        eventMessage.setEventId(this.eventId);
        eventMessage.setEventData(serializer.serialize(event));
        eventMessage.setClassName(event.getClass().getName());
        return eventMessage;
    }

}
