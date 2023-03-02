package org.svnee.easyevent.example.route;

import org.springframework.stereotype.Component;
import org.svnee.easyevent.common.model.Pair;
import org.svnee.easyevent.example.event.TestEvent;
import org.svnee.easyevent.transfer.api.route.EventRouter;

/**
 * @author svnee
 **/
@Component
public class RocketEventRouter implements EventRouter {

    private static final String tag1 = "tag1";
    private static final String tag2 = "tag2";
    private static final String topic = "easyevent";

    @Override
    public Pair<String, String> route(Object event) {
        TestEvent testEvent = (TestEvent) event;
        if (testEvent.getPrice() > 10) {
            return Pair.of(topic, tag2);
        }
        return Pair.of(topic, tag1);
    }
}
