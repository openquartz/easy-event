package org.svnee.easyevent.starter.spring.boot.autoconfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.svnee.easyevent.common.concurrent.ThreadFactoryBuilder;
import org.svnee.easyevent.common.concurrent.TraceThreadPoolExecutor;
import org.svnee.easyevent.common.concurrent.lock.LockSupport;
import org.svnee.easyevent.common.serde.Serializer;
import org.svnee.easyevent.common.transaction.TransactionSupport;
import org.svnee.easyevent.core.DirectInterruptExceptionHandler;
import org.svnee.easyevent.core.EventBus;
import org.svnee.easyevent.core.compensate.EventCompensateService;
import org.svnee.easyevent.core.compensate.EventCompensateServiceImpl;
import org.svnee.easyevent.core.trigger.AsyncEventHandler;
import org.svnee.easyevent.starter.schedule.CompensateProcessGlobalScheduleService;
import org.svnee.easyevent.starter.schedule.CompensateProcessSelfScheduleService;
import org.svnee.easyevent.starter.schedule.ScheduleCompensateRejectedExecutionHandler;
import org.svnee.easyevent.starter.spring.boot.autoconfig.property.DefaultEventBusProperties;
import org.svnee.easyevent.starter.spring.boot.autoconfig.property.EasyEventCommonProperties;
import org.svnee.easyevent.starter.trigger.DefaultAsyncEventHandler;
import org.svnee.easyevent.storage.api.EventStorageService;
import org.svnee.easyevent.transfer.api.EventSender;

/**
 * EasyEvent Creator AutoConfiguration
 *
 * @author svnee
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties(DefaultEventBusProperties.class)
@AutoConfigureAfter(JdbcStorageAutoConfiguration.class)
public class EasyEventCreatorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(EventBus.class)
    public EventBus handleEventBus(ExecutorService defaultEventBusThreadPool) {
        return new EventBus(new DirectInterruptExceptionHandler(), defaultEventBusThreadPool);
    }

    @Bean
    @ConditionalOnMissingBean(name = "defaultEventBusThreadPool")
    public ExecutorService defaultEventBusThreadPool(DefaultEventBusProperties defaultEventBusProperties) {
        return new TraceThreadPoolExecutor(
            defaultEventBusProperties.getThreadPool().getCorePoolSize(),
            defaultEventBusProperties.getThreadPool().getMaximumPoolSize(),
            defaultEventBusProperties.getThreadPool().getKeepAliveTime(),
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(defaultEventBusProperties.getThreadPool().getMaxBlockingQueueSize()),
            new ThreadFactoryBuilder()
                .setNameFormat(defaultEventBusProperties.getThreadPool().getThreadPrefix() + "-thread-%d")
                .build());
    }

    @Bean
    @ConditionalOnMissingBean
    public AsyncEventHandler defaultEventTrigger(EventBus eventBus,
        Serializer serializer,
        EventStorageService eventStorageService,
        TransactionSupport transactionSupport,
        EasyEventCommonProperties easyEventCommonProperties) {
        return new DefaultAsyncEventHandler(eventBus, serializer, eventStorageService, transactionSupport,
            easyEventCommonProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public EventCompensateService eventCompensateService(EventStorageService eventStorageService,
        AsyncEventHandler asyncEventHandler,
        EasyEventCommonProperties easyEventCommonProperties,
        LockSupport lockSupport,
        EventSender eventSender,
        Serializer serializer) {

        TraceThreadPoolExecutor executor = new TraceThreadPoolExecutor(
            easyEventCommonProperties.getCompensate().getThreadPool().getCorePoolSize(),
            easyEventCommonProperties.getCompensate().getThreadPool().getMaximumPoolSize(),
            easyEventCommonProperties.getCompensate().getThreadPool().getKeepAliveTime(),
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(
                easyEventCommonProperties.getCompensate().getThreadPool().getMaxBlockingQueueSize()),
            new ThreadFactoryBuilder()
                .setNameFormat(
                    easyEventCommonProperties.getCompensate().getThreadPool().getThreadPrefix() + "-thread-%d")
                .build());

        return new EventCompensateServiceImpl(eventStorageService, asyncEventHandler, executor, lockSupport,
            eventSender, serializer);
    }

    @Bean
    @ConditionalOnProperty(name = "easyevent.common.compensate.global.enabled", havingValue = "true", matchIfMissing = true)
    public CompensateProcessGlobalScheduleService compensateProcessGlobalScheduleService(
        EasyEventCommonProperties easyEventCommonProperties, EventCompensateService eventCompensateService,
        RejectedExecutionHandler scheduleCompensateRejectedExecutionHandler) {
        return new CompensateProcessGlobalScheduleService(eventCompensateService,
            easyEventCommonProperties,
            scheduleCompensateRejectedExecutionHandler);
    }

    @Bean
    @ConditionalOnMissingBean
    public RejectedExecutionHandler scheduleCompensateRejectedExecutionHandler() {
        return new ScheduleCompensateRejectedExecutionHandler();
    }

    @Bean
    @ConditionalOnProperty(name = "easyevent.common.compensate.self.enabled", havingValue = "true", matchIfMissing = true)
    public CompensateProcessSelfScheduleService compensateProcessSelfScheduleService(
        EasyEventCommonProperties easyEventCommonProperties, EventCompensateService eventCompensateService,
        RejectedExecutionHandler scheduleCompensateRejectedExecutionHandler) {
        return new CompensateProcessSelfScheduleService(eventCompensateService,
            easyEventCommonProperties,
            scheduleCompensateRejectedExecutionHandler);
    }

}
