package com.openquartz.easyevent.admin.model;

import java.util.Date;
import lombok.Data;

@Data
public class BusEventHistoryEntity {
    private Long id;
    private Long entityId;
    private String status;
    private String context;
    private Date createTime;
}
