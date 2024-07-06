package com.openquartz.easyevent.starter.spring.boot.autoconfig;

import com.openquartz.easyevent.core.EventBus;
import com.openquartz.easyevent.core.publisher.EventPublisher;
import com.openquartz.easyevent.starter.processor.EventHandlerPostProcessor;
import com.openquartz.easyevent.starter.processor.InterceptorPostProcessor;
import com.openquartz.easyevent.starter.publisher.DefaultEventPublisher;
import com.openquartz.easyevent.transfer.api.EventSender;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * @author svnee
 **/
@Slf4j
@Configuration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 100000)
public class EasyEventAfterAutoConfiguration {

    public EasyEventAfterAutoConfiguration() {
        log.info("EasyEventAfterAutoConfiguration init >>>>>>>>>>>-------------");
    }

    @Bean
    @ConditionalOnMissingBean
    public EventPublisher defaultEventPublisher(EventBus eventBus, EventSender eventSender) {
        return new DefaultEventPublisher(eventBus, eventSender);
    }

    @Bean
    public InterceptorPostProcessor interceptorPostProcessor() {
        return new InterceptorPostProcessor();
    }

    @Bean
    public EventHandlerPostProcessor eventHandlerPostProcessor(List<EventBus> eventBusList) {
        return new EventHandlerPostProcessor(eventBusList);
    }

}
