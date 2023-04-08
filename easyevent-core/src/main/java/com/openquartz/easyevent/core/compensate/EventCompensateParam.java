package com.openquartz.easyevent.core.compensate;

import java.util.Date;
import java.util.List;
import lombok.Getter;
import com.openquartz.easyevent.storage.model.EventLifecycleState;

/**
 * EventCompensateParamBuilder
 *
 * @author svnee
 **/
@Getter
public class EventCompensateParam {

    /**
     * 状态
     */
    private List<EventLifecycleState> compensateState;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * IP
     */
    private String ipAddr;

    /**
     * 最大异常重试次数
     */
    private Integer maxErrorCount;

    /**
     * 一次处理offset
     */
    private Integer offset;

    private EventCompensateParam() {
    }

    public static EventCompensateParam builder() {
        return new EventCompensateParam();
    }

    public EventCompensateParam compensateState(List<EventLifecycleState> compensateState) {
        this.compensateState = compensateState;
        return this;
    }

    public EventCompensateParam startTime(Date startTime) {
        this.startTime = startTime;
        return this;
    }

    public EventCompensateParam endTime(Date endTime) {
        this.endTime = endTime;
        return this;
    }

    public EventCompensateParam offset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public EventCompensateParam ipAddr(String ipAddr) {
        this.ipAddr = ipAddr;
        return this;
    }

    public EventCompensateParam maxErrorCount(Integer maxErrorCount) {
        this.maxErrorCount = maxErrorCount;
        return this;
    }

    @Override
    public String toString() {
        return "EventCompensateParam{" +
            "compensateState=" + compensateState +
            ", startTime=" + startTime +
            ", endTime=" + endTime +
            ", ipAddr='" + ipAddr + '\'' +
            ", maxErrorCount=" + maxErrorCount +
            ", offset=" + offset +
            '}';
    }
}
