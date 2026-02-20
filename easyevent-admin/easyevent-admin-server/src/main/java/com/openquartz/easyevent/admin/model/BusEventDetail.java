package com.openquartz.easyevent.admin.model;

import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BusEventDetail extends BusEventEntity {
    
    private String title;

    private Integer maxRetries;

    private Date startedTime;

    private Date estimatedCompleteTime;
}
