package com.openquartz.easyevent.storage.model;

import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * EventEntity
 *
 * @author svnee
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class BusEventEntity extends BaseEventEntity {

    /**
     * traceId
     */
    private String traceId;

    /**
     * EventData
     */
    private String eventData;

    /**
     * Event-Key
     */
    private String eventKey;

    /**
     * 创建者机器
     */
    private String creatingOwner;

    /**
     * 生产者机器
     */
    private String processingOwner;

    /**
     * 执行有效时间
     */
    private Date processingAvailableDate;

    /**
     * 已经执行失败的原因
     */
    private String processingFailedReason;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;

}
