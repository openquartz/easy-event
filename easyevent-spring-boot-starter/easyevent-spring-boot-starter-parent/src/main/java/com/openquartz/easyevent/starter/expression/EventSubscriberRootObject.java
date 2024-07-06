package com.openquartz.easyevent.starter.expression;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

/**
 * EventSubscriberRootObject
 *
 * @author svnee
 */
@Slf4j
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

    public EventSubscriberRootObject(Object event,ApplicationContext context, Object[] args) {
        this.event = event;
        this.context = context;
        this.args = args;
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
