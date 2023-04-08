package com.openquartz.easyevent.storage.identify;

import lombok.Data;

/**
 * @author svnee
 **/
@Data
public class EventId {

    /**
     * ID
     */
    private Long id;

    /**
     * 来源ID
     */
    private Long sourceId;

    public EventId() {
    }

    public EventId(Long id, Long sourceId) {
        this.id = id;
        this.sourceId = sourceId;
    }
}
