package com.openquartz.easyevent.admin.model.query;

import java.util.Date;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class EventQuery {
    private Integer page = 1;
    private Integer size = 20;
    
    private Long sourceId;
    private String eventKey;
    private String processingState;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
}
