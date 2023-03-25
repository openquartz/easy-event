package org.svnee.easyevent.example.handler;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.svnee.easyevent.core.annotation.AllowConcurrentEvents;
import org.svnee.easyevent.core.annotation.Subscribe;
import org.svnee.easyevent.core.publisher.EventPublisher;
import org.svnee.easyevent.example.event.TestEvent;
import org.svnee.easyevent.example.event.TestNewEvent;
import org.svnee.easyevent.starter.annotation.EventHandler;

/**
 * @author svnee
 **/
@EventHandler
@RequiredArgsConstructor
public class TestEventHandler {

    private final AtomicInteger count = new AtomicInteger(1);
    private final EventPublisher eventPublisher;

    @Subscribe
    public void handle(TestEvent testEvent) {
        int i = count.incrementAndGet();
        if (i % 2 != 0) {
            throw new RuntimeException("xxxxxx");
        }
    }

    @Subscribe
    public void handle3(TestEvent testEvent) {
        int i = count.incrementAndGet();
        if (i % 3 != 0) {
            throw new RuntimeException("xxxxxx");
        }
    }

    @Subscribe
    @AllowConcurrentEvents
    public void handle2(TestEvent event) {
        TestNewEvent newEvent = new TestNewEvent(event.getPrice());
        eventPublisher.asyncPublish(newEvent);
    }

    @Subscribe
    public void handle4(TestNewEvent event) {
        System.out.println("---+testNew----" + event);
    }
}
