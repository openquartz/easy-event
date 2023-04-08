package com.openquartz.easyevent.example.handler;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import com.openquartz.easyevent.core.annotation.AllowConcurrentEvents;
import com.openquartz.easyevent.core.annotation.Subscribe;
import com.openquartz.easyevent.core.publisher.EventPublisher;
import com.openquartz.easyevent.example.event.TestEvent;
import com.openquartz.easyevent.example.event.TestNewEvent;
import com.openquartz.easyevent.starter.annotation.EventHandler;

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
