package com.openquartz.easyevent.transfer.disruptor.property;

import lombok.Data;

/**
 * Disruptor Trigger Property
 *
 * @author svnee
 **/
@Data
public class DisruptorTriggerProperty {


    /**
     * disruptor-thread-group
     */
    private String disruptorThreadGroup = "easyevent-disruptor";

    /**
     * disruptor-thread-prefix
     */
    private String disruptorThreadPrefix = "disruptor-thread-";

    /**
     * consumer property
     */
    private DisruptorConsumerProperty consumerProperty;

}
