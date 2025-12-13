package com.openquartz.easyevent.example.rabbitmq.controller;

import com.openquartz.easyevent.core.publisher.EventPublisher;
import com.openquartz.easyevent.example.rabbitmq.event.TestEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/event")
public class EventController {

    @Resource
    private EventPublisher eventPublisher;

    @GetMapping("/publish")
    public void publish() {
        eventPublisher.asyncPublish(new TestEvent("test-publish"));
    }

}
