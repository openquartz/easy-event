package org.svnee.easyevent.example.controller;

import javax.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.svnee.easyevent.core.publisher.EventPublisher;
import org.svnee.easyevent.example.event.TestEvent;

/**
 * EventController
 *
 * @author svnee
 **/
@RestController
@RequestMapping("/event")
public class EventController {

    @Resource
    private EventPublisher eventPublisher;

    @RequestMapping("/sync/publish")
    public void syncPublish() {

        TestEvent testEvent = new TestEvent(10);
        eventPublisher.syncPublish(testEvent);
    }


    @RequestMapping("/async/publish")
    public void asyncPublish() {

        TestEvent testEvent = new TestEvent(11);
        eventPublisher.asyncPublish(testEvent);
    }

}
