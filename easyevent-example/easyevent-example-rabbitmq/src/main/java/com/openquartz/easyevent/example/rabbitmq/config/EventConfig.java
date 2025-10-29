package com.openquartz.easyevent.example.rabbitmq.config;

import com.openquartz.easyevent.core.EventBus;
import com.openquartz.easyevent.example.rabbitmq.handler.TestEventHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 事件配置
 *
 * @author svnee
 */
@Configuration
public class EventConfig {

    @Bean
    public EventBus eventBus() {
        EventBus eventBus = new EventBus();
        eventBus.register(new TestEventHandler());
        return eventBus;
    }
}