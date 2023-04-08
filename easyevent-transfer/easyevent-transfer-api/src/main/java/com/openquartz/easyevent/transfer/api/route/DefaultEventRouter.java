package com.openquartz.easyevent.transfer.api.route;

import com.openquartz.easyevent.common.model.Pair;

/**
 * 默认事件路由策略
 *
 * @author svnee
 **/
public class DefaultEventRouter implements EventRouter {

    /**
     * 默认路由topic
     */
    private final String defaultRouteTopic;

    public DefaultEventRouter(String defaultRouteTopic) {
        this.defaultRouteTopic = defaultRouteTopic;
    }

    @Override
    public Pair<String, String> route(Object event) {
        return Pair.of(defaultRouteTopic, null);
    }
}
