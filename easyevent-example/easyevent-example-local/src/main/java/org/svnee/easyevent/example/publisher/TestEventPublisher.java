package org.svnee.easyevent.example.publisher;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;
import org.svnee.easyevent.core.publisher.EventPublisher;
import org.svnee.easyevent.example.event.TestEvent;

/**
 * @author svnee
 **/
@Component
public class TestEventPublisher {

    @Resource
    private EventPublisher eventPublisher;

    public void publish(TestEvent testEvent) {
        eventPublisher.syncPublish(testEvent);
    }

    public void asyncPublish(TestEvent event) {
        eventPublisher.asyncPublish(event);
    }

    public void asyncPublishList(List<TestEvent> eventList) {
        ArrayList<Object> list = new ArrayList<>(eventList);
        eventPublisher.asyncPublishList(list);
    }

}
