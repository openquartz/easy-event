package com.openquartz.easyevent.starter.init;

import com.openquartz.easyevent.starter.spring.boot.autoconfig.property.EasyEventCommonProperties;
import lombok.extern.slf4j.Slf4j;
import com.openquartz.easyevent.common.model.LifecycleBean;
import com.openquartz.easyevent.core.logo.EasyEventBannerPrinter;

/**
 * @author svnee
 **/
@Slf4j
public class EasyEventInitializingEntrance implements LifecycleBean {

    public final EasyEventCommonProperties properties;

    public EasyEventInitializingEntrance(
        EasyEventCommonProperties properties) {
        this.properties = properties;
    }

    /**
     * init method
     */
    @Override
    public void init() {

        // print banner
        new EasyEventBannerPrinter().print(log);
    }

    /**
     * destroy method
     */
    @Override
    public void destroy() {

    }
}
