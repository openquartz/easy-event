package org.svnee.easyevent.transfer.disruptor.common;

import com.lmax.disruptor.EventTranslatorOneArg;
import org.svnee.easyevent.transfer.api.message.EventMessage;
import org.svnee.easyevent.transfer.disruptor.event.DisruptorTriggerEvent;

/**
 * DisruptorTriggerEventTranslator
 *
 * @author svnee
 */
public class DisruptorTriggerEventTranslator implements EventTranslatorOneArg<DisruptorTriggerEvent, EventMessage> {

    @Override
    public void translateTo(final DisruptorTriggerEvent event, final long l, final EventMessage message) {
        event.setEventId(message.getEventId());
        event.setEventData(message.getEventData());
        event.setClassName(message.getClassName());
    }
}
