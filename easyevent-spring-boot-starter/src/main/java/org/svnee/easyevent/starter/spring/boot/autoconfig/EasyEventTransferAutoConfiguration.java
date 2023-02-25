package org.svnee.easyevent.starter.spring.boot.autoconfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.svnee.easyevent.common.concurrent.ThreadFactoryBuilder;
import org.svnee.easyevent.common.concurrent.TraceThreadPoolExecutor;
import org.svnee.easyevent.starter.spring.boot.autoconfig.property.DefaultTransferProperties;
import org.svnee.easyevent.starter.spring.boot.autoconfig.property.DefaultTransferSenderProperties;
import org.svnee.easyevent.transfer.api.limiting.EventTransferSenderLimitingControl;
import org.svnee.easyevent.transfer.api.limiting.EventTransferTriggerLimitingControl;
import org.svnee.easyevent.transfer.api.limiting.impl.DefaultEventTransferSenderLimitingControl;
import org.svnee.easyevent.transfer.api.limiting.impl.DefaultEventTransferTriggerLimitingControl;
import org.svnee.easyevent.transfer.api.route.DefaultEventRouter;
import org.svnee.easyevent.transfer.api.route.EventRouter;

/**
 * EasyEvent Transfer  AutoConfiguration
 *
 * @author svnee
 **/
@AutoConfigureAfter(EasyEventCreatorAutoConfiguration.class)
@EnableConfigurationProperties({DefaultTransferSenderProperties.class, DefaultTransferProperties.class})
public class EasyEventTransferAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "asyncSendExecutor")
    public ExecutorService asyncSendExecutor(DefaultTransferSenderProperties properties) {
        return new TraceThreadPoolExecutor(
            properties.getThreadPool().getCorePoolSize(),
            properties.getThreadPool().getMaximumPoolSize(),
            properties.getThreadPool().getKeepAliveTime(),
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(properties.getThreadPool().getMaxBlockingQueueSize()),
            new ThreadFactoryBuilder()
                .setNameFormat(properties.getThreadPool().getThreadPrefix() + "-thread-%d")
                .build());
    }

    @Bean
    @ConditionalOnMissingBean
    public EventRouter eventRouteStrategy(DefaultTransferProperties properties) {
        return new DefaultEventRouter(properties.getDefaultTopic());
    }

    @Bean
    @ConditionalOnMissingBean
    public EventTransferSenderLimitingControl eventTransferSenderLimitingControl() {
        return new DefaultEventTransferSenderLimitingControl();
    }

    @Bean
    @ConditionalOnMissingBean
    public EventTransferTriggerLimitingControl eventTransferTriggerLimitingControl() {
        return new DefaultEventTransferTriggerLimitingControl();
    }
}
