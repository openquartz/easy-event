package org.svnee.easyevent.common.concurrent;

/**
 * TraceContextParam
 *
 * @author svnee
 */
public enum TraceContextParam {

    TRACE_ID("traceId"),
    SOURCE_EVENT_ID("sourceEventId"),
    ;

    TraceContextParam(String code) {
        this.code = code;
    }

    /**
     * Code
     */
    private final String code;

    public String getCode() {
        return code;
    }
}
