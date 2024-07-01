package com.openquartz.easyevent.starter.soa.core;

import com.openquartz.easyevent.core.annotation.Subscribe;

/**
 * SoaEventHandler
 *
 * @author svnee
 */
public class SoaEventHandler {

    private final SoaEventCenter soaEventCenter;

    public SoaEventHandler(SoaEventCenter soaEventCenter) {
        this.soaEventCenter = soaEventCenter;
    }

    @Subscribe(condition = "#event.soaIdentify!=@easyEventProperties.getAppId()")
    public void handle(SoaEvent event) {
        soaEventCenter.produce(event);
    }

}
