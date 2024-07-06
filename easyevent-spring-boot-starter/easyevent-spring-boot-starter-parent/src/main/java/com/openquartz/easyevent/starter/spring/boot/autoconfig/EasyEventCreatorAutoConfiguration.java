package com.openquartz.easyevent.starter.spring.boot.autoconfig;

import com.openquartz.easyevent.common.concurrent.ThreadFactoryBuilder;
import com.openquartz.easyevent.common.concurrent.TraceThreadPoolExecutor;
import com.openquartz.easyevent.common.concurrent.lock.LockSupport;
import com.openquartz.easyevent.common.serde.Serializer;
import com.openquartz.easyevent.common.transaction.TransactionSupport;
import com.openquartz.easyevent.core.DirectInterruptExceptionHandler;
import com.openquartz.easyevent.core.EventBus;
import com.openquartz.easyevent.core.compensate.EventCompensateService;
import com.openquartz.easyevent.core.compensate.EventCompensateServiceImpl;
import com.openquartz.easyevent.core.expression.ExpressionParser;
import com.openquartz.easyevent.core.notify.EventHandleFailedNotifier;
import com.openquartz.easyevent.core.notify.EventNotifier;
import com.openquartz.easyevent.core.notify.LogEventNotifier;
import com.openquartz.easyevent.core.trigger.AsyncEventHandler;
import com.openquartz.easyevent.starter.expression.SpringExpressionParser;
import com.openquartz.easyevent.starter.schedule.CompensateProcessGlobalScheduleService;
import com.openquartz.easyevent.starter.schedule.CompensateProcessSelfScheduleService;
import com.openquartz.easyevent.starter.schedule.DefaultEventHandleFailedNotifier;
import com.openquartz.easyevent.starter.schedule.ScheduleCompensateRejectedExecutionHandler;
import com.openquartz.easyevent.starter.spring.boot.autoconfig.property.DefaultEventBusProperties;
import com.openquartz.easyevent.starter.spring.boot.autoconfig.property.EasyEventCommonProperties;
import com.openquartz.easyevent.starter.trigger.DefaultAsyncEventHandler;
import com.openquartz.easyevent.storage.api.EventStorageService;
import com.openquartz.easyevent.transfer.api.EventSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.TimeUnit;

/**
 * EasyEvent Creator AutoConfiguration
 *
 * @author svnee
 **/
@Slf4j
@Configuration
@EnableConfigurationProperties(DefaultEventBusProperties.class)
@AutoConfigureAfter(JdbcStorageAutoConfiguration.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 1000)
public class EasyEventCreatorAutoConfiguration {

    public EasyEventCreatorAutoConfiguration(){
        log.info("EasyEventCreatorAutoConfiguration init>>>>>>>>>---------------");
    }

    @Bean
    @ConditionalOnMissingBean(EventBus.class)
    public EventBus handleEventBus(@Qualifier("defaultEventBusThreadPool") ExecutorService defaultEventBusThreadPool,
                                   ExpressionParser expressionParser) {
        return new EventBus(new DirectInterruptExceptionHandler(), defaultEventBusThreadPool, expressionParser);
    }

    @Bean
    @ConditionalOnMissingBean(SpelExpressionParser.class)
    public SpelExpressionParser spelExpressionParser() {
        return new SpelExpressionParser();
    }

    @Bean
    @ConditionalOnMissingBean(ExpressionParser.class)
    public ExpressionParser springExpressionParser(SpelExpressionParser spelExpressionParser) {
        return new SpringExpressionParser(spelExpressionParser);
    }

    @Bean(name = "defaultEventBusThreadPool")
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

    @Bean
    @ConditionalOnProperty(name = "easyevent.common.notify.enabled", havingValue = "true", matchIfMissing = true)
    public EventHandleFailedNotifier eventHandleFailedNotifier(EasyEventCommonProperties easyEventCommonProperties,
                                                               EventStorageService eventStorageService,
                                                               EventNotifier eventNotifier,
                                                               LockSupport lockSupport) {
        return new DefaultEventHandleFailedNotifier(eventStorageService, easyEventCommonProperties.getMaxRetryCount(),
                eventNotifier,
                easyEventCommonProperties.getNotify(),
                lockSupport);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "easyevent.common.notify.enabled", havingValue = "true", matchIfMissing = true)
    public EventNotifier eventNotifier() {
        return new LogEventNotifier();
    }

}
