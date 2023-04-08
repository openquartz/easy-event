package com.openquartz.easyevent.transfer.api.limiting.impl;

import com.openquartz.easyevent.transfer.api.limiting.EventTransferTriggerLimitingControl;
import com.openquartz.easyevent.transfer.api.message.EventMessage;
import java.util.function.Consumer;

/**
 * Default EventTransferTriggerLimitingControl
 *
 * @author svnee
 **/
public class DefaultEventTransferTriggerLimitingControl implements EventTransferTriggerLimitingControl {

    @Override
    public void control(EventMessage eventMessage, Consumer<EventMessage> eventHandleFunction) {
        eventHandleFunction.accept(eventMessage);
    }
}
