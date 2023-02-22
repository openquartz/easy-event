package org.svnee.easyevent.starter.init;

import lombok.extern.slf4j.Slf4j;
import org.svnee.easyevent.common.model.LifecycleBean;
import org.svnee.easyevent.core.logo.EasyEventBannerPrinter;
import org.svnee.easyevent.starter.spring.boot.autoconfig.property.EasyEventCommonProperties;

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
