package org.svnee.easyevent.example.handler;

import org.svnee.easyevent.core.annotation.AllowConcurrentEvents;
import org.svnee.easyevent.core.annotation.Subscribe;
import org.svnee.easyevent.example.event.KafkaTestEvent;
import org.svnee.easyevent.starter.annotation.EventHandler;

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
