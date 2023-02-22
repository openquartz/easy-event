package org.svnee.easyevent.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.svnee.easyevent.common.utils.CollectionUtils;
import org.svnee.easyevent.example.event.TestEvent;
import org.svnee.easyevent.example.publisher.TestEventPublisher;

/**
 * @author svnee
 **/
@RestController
@RequestMapping("/event")
public class EventController {

    @Autowired
    private TestEventPublisher testEventPublisher;

    @RequestMapping("/sync")
    public void publish() {

        TestEvent source = new TestEvent("source");
        testEventPublisher.publish(source);
    }

    @RequestMapping("/async")
    public void asyncPublish() {

        TestEvent source = new TestEvent("source");
        testEventPublisher.asyncPublish(source);
    }

    @RequestMapping("/async-batch")
    public void batchAsyncPublish() {
        TestEvent source1 = new TestEvent("source1");
        TestEvent source2 = new TestEvent("source2");

        testEventPublisher.asyncPublishList(CollectionUtils.newArrayList(source1, source2));
    }

}
