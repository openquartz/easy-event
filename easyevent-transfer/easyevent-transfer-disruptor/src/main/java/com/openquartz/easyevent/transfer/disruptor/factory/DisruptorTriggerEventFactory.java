package com.openquartz.easyevent.transfer.disruptor.factory;

import com.lmax.disruptor.EventFactory;
import com.openquartz.easyevent.transfer.disruptor.event.DisruptorTriggerEvent;

/**
 * DisruptorTriggerEventFactory
 *
 * @author svnee
 **/
public class DisruptorTriggerEventFactory implements EventFactory<DisruptorTriggerEvent> {

    @Override
    public DisruptorTriggerEvent newInstance() {
        return new DisruptorTriggerEvent();
    }
}
