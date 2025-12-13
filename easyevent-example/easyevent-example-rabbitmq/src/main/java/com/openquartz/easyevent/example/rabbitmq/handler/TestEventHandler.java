package com.openquartz.easyevent.example.rabbitmq.handler;

import com.openquartz.easyevent.core.annotation.Subscribe;
import com.openquartz.easyevent.example.rabbitmq.event.TestEvent;
import com.openquartz.easyevent.starter.annotation.EventHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 测试事件处理器
 *
 * @author svnee
 */
@Slf4j
@EventHandler
public class TestEventHandler {

    @Subscribe
    public void handleTestEvent(TestEvent event) {
        log.info("Received test event: {}", event);
        // 处理业务逻辑
        System.out.println("处理测试事件: " + event.getMessage() + ", 时间戳: " + event.getTimestamp());
    }
}