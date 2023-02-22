package org.svnee.easyevent.starter.spring.boot.autoconfig;

import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.svnee.easyevent.core.EventBus;
import org.svnee.easyevent.core.publisher.EventPublisher;
import org.svnee.easyevent.starter.processor.EventHandlerPostProcessor;
import org.svnee.easyevent.starter.processor.InterceptorPostProcessor;
import org.svnee.easyevent.starter.publisher.DefaultEventPublisher;
import org.svnee.easyevent.transfer.api.EventSender;

/**
 * @author svnee
 **/
@Configuration
@AutoConfigureAfter(RocketMqTransferAutoConfiguration.class)
public class EasyEventAfterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EventPublisher defaultEventPublisher(EventBus eventBus,
        EventSender eventSender) {
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
