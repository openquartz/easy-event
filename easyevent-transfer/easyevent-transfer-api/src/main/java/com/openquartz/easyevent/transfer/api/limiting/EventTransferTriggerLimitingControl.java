package com.openquartz.easyevent.transfer.api.limiting;

import java.util.function.Consumer;
import com.openquartz.easyevent.transfer.api.message.EventMessage;

/**
 * EventTransfer Trigger Limiting Control
 *
 * @author svnee
 */
public interface EventTransferTriggerLimitingControl {

    /**
     * control
     * if limiting blocked throw {@link LimitingBlockedException}
     *
     * @param eventMessage event-message
     * @param eventHandleFunction function
     */
    void control(EventMessage eventMessage, Consumer<EventMessage> eventHandleFunction);

}
