package com.openquartz.easyevent.starter.expression;

/**
 * EventSubscriberRootObject
 * @author svnee
 */
public class EventSubscriberRootObject {

    /**
     * event
     */
    private final Object event;

    /**
     * args
     */
    private final Object[] args;

    public EventSubscriberRootObject(Object event, Object[] args) {
        this.event = event;
        this.args = args;
    }

    public Object getEvent() {
        return event;
    }

    public Object[] getArgs() {
        return args;
    }
}
