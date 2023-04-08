package com.openquartz.easyevent.storage.model;

/**
 * EventEntity
 *
 * @author svnee
 */
public enum EventLifecycleState {
    AVAILABLE("AVAILABLE", "待处理"),
    TRANSFER_SUCCESS("TRANSFER_SUCCESS", "触发成功"),
    TRANSFER_FAILED("TRANSFER_FAILED", "触发失败"),
    IN_PROCESSING("IN_PROCESSING", "处理中"),
    PROCESS_COMPLETE("PROCESS_COMPLETE", "处理完成"),
    PROCESS_FAILED("PROCESS_FAILED", "处理失败"),
    ;
    private final String code;
    private final String desc;

    EventLifecycleState(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static EventLifecycleState of(String code) {
        for (EventLifecycleState value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return "EventLifecycleState{" +
            "code='" + code + '\'' +
            ", desc='" + desc + '\'' +
            '}';
    }
}
