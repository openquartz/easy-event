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
    private static final String tag3 = "tag3";
    private static final String topic = "easyevent";
    private static final String topic_1 = "easyevent_1";

    @Override
    public Pair<String, String> route(Object event) {
        TestEvent testEvent = (TestEvent) event;
        if (testEvent.getPrice() > 10) {
            if (testEvent.getPrice() % 2 == 0) {
                return Pair.of(topic, tag2);
            } else {
                return Pair.of(topic_1, tag3);
            }
        }
        return Pair.of(topic, tag1);
    }
}
