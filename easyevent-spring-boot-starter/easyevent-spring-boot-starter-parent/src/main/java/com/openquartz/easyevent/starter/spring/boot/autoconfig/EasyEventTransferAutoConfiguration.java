package com.openquartz.easyevent.starter.spring.boot.autoconfig;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.openquartz.easyevent.common.concurrent.ThreadFactoryBuilder;
import com.openquartz.easyevent.common.concurrent.TraceThreadPoolExecutor;
import com.openquartz.easyevent.starter.spring.boot.autoconfig.property.DefaultTransferProperties;
import com.openquartz.easyevent.starter.spring.boot.autoconfig.property.DefaultTransferSenderProperties;
import com.openquartz.easyevent.transfer.api.limiting.EventTransferSenderLimitingControl;
import com.openquartz.easyevent.transfer.api.limiting.EventTransferTriggerLimitingControl;
import com.openquartz.easyevent.transfer.api.limiting.impl.DefaultEventTransferSenderLimitingControl;
import com.openquartz.easyevent.transfer.api.limiting.impl.DefaultEventTransferTriggerLimitingControl;
import com.openquartz.easyevent.transfer.api.route.DefaultEventRouter;
import com.openquartz.easyevent.transfer.api.route.EventRouter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * EasyEvent Transfer  AutoConfiguration
 *
 * @author svnee
 **/
@Slf4j
@AutoConfigureAfter(EasyEventCreatorAutoConfiguration.class)
@EnableConfigurationProperties({DefaultTransferSenderProperties.class, DefaultTransferProperties.class})
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 10000)
public class EasyEventTransferAutoConfiguration {

    public EasyEventTransferAutoConfiguration() {
        log.info("EasyEventTransferAutoConfiguration init>>>>>>>>-----------");
    }

    @Bean(name = "asyncSendExecutor")
    @ConditionalOnMissingBean(name = "asyncSendExecutor")
    public ExecutorService asyncSendExecutor(DefaultTransferSenderProperties properties) {

        return TtlExecutors.getTtlExecutorService(new TraceThreadPoolExecutor(
                properties.getThreadPool().getCorePoolSize(),
                properties.getThreadPool().getMaximumPoolSize(),
                properties.getThreadPool().getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(properties.getThreadPool().getMaxBlockingQueueSize()),
                new ThreadFactoryBuilder()
                        .setNameFormat(properties.getThreadPool().getThreadPrefix() + "-thread-%d")
                        .build()));
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
