package com.openquartz.easyevent.starter.soa.core;

/**
 * SoaEventCenter
 *
 * @author svnee
 */
public interface SoaEventCenter {

    /**
     * produce soa-event
     *
     * @param event event
     */
    void produce(SoaEvent event);

    /**
     * 消费SoaEvent
     *
     * @param event event
     */
    void consume(SoaEvent event);

}
