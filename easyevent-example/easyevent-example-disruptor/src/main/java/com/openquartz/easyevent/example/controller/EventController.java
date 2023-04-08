package com.openquartz.easyevent.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.openquartz.easyevent.common.utils.CollectionUtils;
import com.openquartz.easyevent.example.event.TestEvent;
import com.openquartz.easyevent.example.publisher.TestEventPublisher;

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
