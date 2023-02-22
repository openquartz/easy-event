package org.svnee.easyevent.transfer.api.route;

import org.svnee.easyevent.common.model.Pair;
import org.svnee.easyevent.transfer.api.constant.TransferConstants;

/**
 * 默认事件路由策略
 *
 * @author svnee
 **/
public class DefaultEventRouteStrategy implements EventRouteStrategy {

    /**
     * 默认路由topic
     */
    private final String defaultRouteTopic;

    public DefaultEventRouteStrategy(String defaultRouteTopic) {
        this.defaultRouteTopic = defaultRouteTopic;
    }

    @Override
    public Pair<String, String> route(Object event) {
        return Pair.of(defaultRouteTopic, TransferConstants.DEFAULT_TAG);
    }
}
