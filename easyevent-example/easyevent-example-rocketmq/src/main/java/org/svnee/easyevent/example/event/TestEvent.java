package org.svnee.easyevent.example.event;

import lombok.Data;

/**
 * @author svnee
 **/
@Data
public class TestEvent {

    private Integer price;

    public TestEvent() {
    }

    public TestEvent(Integer price) {
        this.price = price;
    }
}
