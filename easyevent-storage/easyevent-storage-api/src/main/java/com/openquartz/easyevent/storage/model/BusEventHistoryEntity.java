package com.openquartz.easyevent.storage.model;

import java.util.Date;
import lombok.Data;

/**
 * Event History Entity
 */
@Data
public class BusEventHistoryEntity {

    private Long id;
    private Long entityId;
    private String status;
    private String context;
    private Date createTime;
}
