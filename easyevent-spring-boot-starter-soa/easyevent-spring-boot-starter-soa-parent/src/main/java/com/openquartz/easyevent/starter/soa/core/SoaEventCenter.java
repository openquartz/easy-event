package com.openquartz.easyevent.starter.soa.core;

import com.openquartz.easyevent.starter.soa.api.SoaEvent;

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
    void publish(SoaEvent event);

    /**
     * 消费SoaEvent
     *
     * @param event event
     */
    void subscribe(SoaEvent event);

}
