package com.openquartz.easyevent.storage.model;

import lombok.Data;

@Data
public class EventBody<T> {

    private T event;

    private EventContext context;

    public EventBody() {
    }

    public EventBody(T event, EventContext context) {
        this.event = event;
        this.context = context;
    }
}
