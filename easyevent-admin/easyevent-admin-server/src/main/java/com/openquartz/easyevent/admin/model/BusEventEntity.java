package com.openquartz.easyevent.admin.model;

import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class BusEventEntity {

    private Long id;

    private String appId;

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

    private List<BusEventHistoryEntity> statusHistory;
}
