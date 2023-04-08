package com.openquartz.easyevent.transfer.api.message;

import lombok.Data;
import com.openquartz.easyevent.storage.identify.EventId;

/**
 * 事件消息体
 *
 * @author svnee
 **/
@Data
public class EventMessage {

    private EventId eventId;
    private String className;
    private String eventData;

}
