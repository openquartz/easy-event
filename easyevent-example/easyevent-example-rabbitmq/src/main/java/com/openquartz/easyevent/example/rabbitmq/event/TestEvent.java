package com.openquartz.easyevent.example.rabbitmq.event;

import lombok.Data;

/**
 * 测试事件
 *
 * @author svnee
 */
@Data
public class TestEvent {

    private String message;
    private Long timestamp;

    public TestEvent() {
    }

    public TestEvent(String message) {
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
}