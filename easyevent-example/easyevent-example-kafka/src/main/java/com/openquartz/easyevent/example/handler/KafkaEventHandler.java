package com.openquartz.easyevent.example.handler;

import com.openquartz.easyevent.core.annotation.AllowConcurrentEvents;
import com.openquartz.easyevent.core.annotation.Subscribe;
import com.openquartz.easyevent.example.event.KafkaTestEvent;
import com.openquartz.easyevent.starter.annotation.EventHandler;

/**
 * @author svnee
 **/
@EventHandler
public class KafkaEventHandler {

    @Subscribe
    @AllowConcurrentEvents
    public void handle(KafkaTestEvent kafkaTestEvent) {
        if (kafkaTestEvent.getRandom() > 1) {
            System.out.println("kafka event handle");
        } else {
            throw new RuntimeException("xxxxxxxxx");
        }
    }

    @Subscribe
    @AllowConcurrentEvents
    public void handle2(KafkaTestEvent kafkaTestEvent) {
        if (kafkaTestEvent.getRandom() > 1) {
            System.out.println("kafka event handle");
        } else {
            throw new RuntimeException("xxxxxxxxx");
        }
    }

    @Subscribe
    public void handle3(KafkaTestEvent kafkaTestEvent) {
        if (kafkaTestEvent.getRandom() > 1) {
            System.out.println("kafka event handle");
        } else {
            throw new RuntimeException("xxxxxxxxx");
        }
    }

    @Subscribe
    public void handle4(KafkaTestEvent kafkaTestEvent) {
        if (kafkaTestEvent.getRandom() > 1) {
            System.out.println("kafka event handle");
        } else {
            throw new RuntimeException("xxxxxxxxx");
        }

    }
}
