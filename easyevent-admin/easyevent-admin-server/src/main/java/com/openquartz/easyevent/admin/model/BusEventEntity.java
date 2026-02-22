package com.openquartz.easyevent.admin.model;

import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class BusEventEntity {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String appId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long sourceId;

    private String className;

    private Integer errorCount;

    private String processingState;

    private String successfulSubscriber;

    private String traceId;

    private String eventData;

    private String eventKey;

    private String creatingOwner;

    private String processingOwner;

    private Date processingAvailableDate;

    private String processingFailedReason;

    private Date createdTime;

    private Date updatedTime;

    private Date startExecutionTime;

    private Date executionSuccessTime;


}
