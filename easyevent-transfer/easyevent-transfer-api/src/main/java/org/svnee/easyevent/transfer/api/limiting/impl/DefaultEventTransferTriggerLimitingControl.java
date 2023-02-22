package org.svnee.easyevent.transfer.api.limiting.impl;

import java.util.function.Consumer;
import org.svnee.easyevent.transfer.api.limiting.EventTransferTriggerLimitingControl;
import org.svnee.easyevent.transfer.api.message.EventMessage;

/**
 * Default impl
 *
 * @author svnee
 **/
public class DefaultEventTransferTriggerLimitingControl implements EventTransferTriggerLimitingControl {

    @Override
    public void control(EventMessage eventMessage, Consumer<EventMessage> eventHandleFunction) {
        eventHandleFunction.accept(eventMessage);
    }
}
