package org.svnee.easyevent.example.event;

import lombok.Data;

/**
 * Kafka TestEvent
 *
 * @author svnee
 **/
@Data
public class KafkaTestEvent {

    private int random;

    public KafkaTestEvent() {
    }

    public KafkaTestEvent(int random) {
        this.random = random;
    }
}
