package org.svnee.easyevent.transfer.api.message;

import lombok.Data;
import org.svnee.easyevent.storage.identify.EventId;

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
