package com.openquartz.easyevent.storage.model;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import com.openquartz.easyevent.common.model.Range;

/**
 * BusEventSelectorCondition
 *
 * @author svnee
 **/
@Getter
public final class BusEventSelectorCondition {

    private final String appId;
    private List<EventLifecycleState> lifecycleStateList;
    private List<String> creatingOwnerList;
    private Range<Date> createTimeRange;
    private Integer maxErrorCount;
    private Integer minErrorCount;
    private final Integer offset;

    private BusEventSelectorCondition(String appId, Integer offset) {
        this.offset = offset;
        this.appId = appId;
    }

    public static BusEventSelectorCondition builder(String appId, Integer offset) {
        return new BusEventSelectorCondition(appId, offset);
    }

    public BusEventSelectorCondition lifecycleState(List<EventLifecycleState> lifecycleStateList) {
        this.lifecycleStateList = lifecycleStateList;
        return this;
    }

    public BusEventSelectorCondition creatingOwner(List<String> creatingOwnerList) {
        this.creatingOwnerList = creatingOwnerList;
        return this;
    }

    public BusEventSelectorCondition start(Date startTime) {
        if (startTime == null) {
            return this;
        }
        if (createTimeRange == null) {
            createTimeRange = new Range<>(startTime, null);
        } else {
            createTimeRange.start(startTime);
        }
        return this;
    }

    public BusEventSelectorCondition end(Date endTime) {
        if (endTime == null) {
            return this;
        }
        if (createTimeRange == null) {
            createTimeRange = new Range<>(null, endTime);
        } else {
            createTimeRange.end(endTime);
        }
        return this;
    }

    public BusEventSelectorCondition maxErrorCount(Integer maxErrorCount) {
        this.maxErrorCount = maxErrorCount;
        return this;
    }

    public BusEventSelectorCondition minErrorCount(Integer minErrorCount) {
        this.minErrorCount = minErrorCount;
        return this;
    }
}
