package org.svnee.easyevent.common.model;

/**
 * Event Trace StateInfo
 *
 * @author svnee
 **/
public class EventTraceStateInfo {

    private Class<?> eventType;
    private Long eventId;
    private Long eventSourceId;

    public Class<?> getEventType() {
        return eventType;
    }

    public void setEventType(Class<?> eventType) {
        this.eventType = eventType;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getEventSourceId() {
        return eventSourceId;
    }

    public void setEventSourceId(Long eventSourceId) {
        this.eventSourceId = eventSourceId;
    }
}
