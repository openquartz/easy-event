package com.openquartz.easyevent.starter.expression;

import org.springframework.context.ApplicationContext;

/**
 * EventSubscriberRootObject
 *
 * @author svnee
 */
public class EventSubscriberRootObject {

    /**
     * event
     */
    private final Object event;

    /**
     * application context
     */
    private final ApplicationContext context;

    /**
     * args
     */
    private final Object[] args;

    public EventSubscriberRootObject(Object event, ApplicationContext context, Object[] args) {
        this.event = event;
        this.args = args;
        this.context = context;
    }

    public ApplicationContext getContext() {
        return context;
    }

    public Object getEvent() {
        return event;
    }

    public Object[] getArgs() {
        return args;
    }
}
