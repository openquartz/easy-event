package org.svnee.easyevent.example.controller;

import javax.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.svnee.easyevent.common.utils.CollectionUtils;
import org.svnee.easyevent.core.publisher.EventPublisher;
import org.svnee.easyevent.example.event.KafkaTestEvent;

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

        KafkaTestEvent testEvent = new KafkaTestEvent(10);
        eventPublisher.syncPublish(testEvent);
    }


    @RequestMapping("/async/publish")
    public void asyncPublish() {

        KafkaTestEvent testEvent = new KafkaTestEvent(11);
        eventPublisher.asyncPublish(testEvent);
    }

    @RequestMapping("/async/publish/list")
    public void asyncPublishList() {

        KafkaTestEvent testEvent1 = new KafkaTestEvent(11);
        KafkaTestEvent testEvent2 = new KafkaTestEvent(12);
        KafkaTestEvent testEvent3 = new KafkaTestEvent(9);
        eventPublisher.asyncPublishList(CollectionUtils.newArrayList(testEvent1, testEvent2, testEvent3));
    }
}
