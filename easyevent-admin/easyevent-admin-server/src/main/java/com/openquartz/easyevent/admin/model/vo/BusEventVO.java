package com.openquartz.easyevent.admin.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.openquartz.easyevent.admin.model.BusEventHistoryEntity;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class BusEventVO {

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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date processingAvailableDate;

    private String processingFailedReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startExecutionTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date executionSuccessTime;

    private List<BusEventHistoryVO> statusHistory;
}
