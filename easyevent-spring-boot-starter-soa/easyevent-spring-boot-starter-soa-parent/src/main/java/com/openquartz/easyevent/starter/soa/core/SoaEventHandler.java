package com.openquartz.easyevent.starter.soa.core;

import com.openquartz.easyevent.core.annotation.Subscribe;
import com.openquartz.easyevent.starter.soa.api.SoaEvent;

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

    @Subscribe(condition = "event.getSoaIdentify()==context.getBean(T(com.openquartz.easyevent.common.property.EasyEventProperties)).getAppId()")
    public void handle(SoaEvent event) {
        soaEventCenter.publish(event);
    }

}
