package com.openquartz.easyevent.starter.schedule;

import java.util.Date;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.InitializingBean;
import com.openquartz.easyevent.common.concurrent.ThreadFactoryBuilder;
import com.openquartz.easyevent.common.utils.DateUtils;
import com.openquartz.easyevent.core.compensate.EventCompensateParam;
import com.openquartz.easyevent.core.compensate.EventCompensateService;
import com.openquartz.easyevent.starter.spring.boot.autoconfig.property.EasyEventCommonProperties;
import com.openquartz.easyevent.starter.spring.boot.autoconfig.property.EasyEventCommonProperties.EventCompensateProcessProperty;

/**
 * CompensateProcessGlobalScheduleService
 *
 * @author svnee
 **/
public class CompensateProcessGlobalScheduleService implements InitializingBean {

    private final EventCompensateService eventCompensateService;
    private final EasyEventCommonProperties easyEventCommonProperties;
    private final ScheduledThreadPoolExecutor selfScheduledThreadPool;

    private ScheduledThreadPoolExecutor init(EventCompensateProcessProperty property,
        RejectedExecutionHandler rejectedExecutionHandler) {
        return new ScheduledThreadPoolExecutor(property.getThreadPoolCoreSize(),
            new ThreadFactoryBuilder()
                .setNameFormat(property.getThreadPoolThreadPrefix() + "-global-thread-%d")
                .build(),
            rejectedExecutionHandler);
    }

    public CompensateProcessGlobalScheduleService(
        EventCompensateService eventCompensateService,
        EasyEventCommonProperties easyEventCommonProperties,
        RejectedExecutionHandler scheduledCompensateRejectedExecutionHandler) {

        this.eventCompensateService = eventCompensateService;
        this.easyEventCommonProperties = easyEventCommonProperties;
        this.selfScheduledThreadPool = init(easyEventCommonProperties.getCompensate().getGlobal(),
            scheduledCompensateRejectedExecutionHandler);
    }

    private void doGlobalProcess() {
        Date now = new Date();
        // Window: [now - beforeEndSeconds, now - beforeStartSeconds)
        // e.g. defaults (beforeStart=60, beforeEnd=3600) => events aged 60s~1h.
        Integer beforeStartSeconds = easyEventCommonProperties.getCompensate().getGlobal().getBeforeStartSeconds();
        Integer beforeEndSeconds = easyEventCommonProperties.getCompensate().getGlobal().getBeforeEndSeconds();
        EventCompensateParam param = EventCompensateParam.builder(easyEventCommonProperties.getAppId())
            .compensateState(easyEventCommonProperties.getCompensate().getGlobal().getCompensateState())
            .offset(easyEventCommonProperties.getCompensate().getGlobal().getOffset())
            .startTime(DateUtils.addSeconds(now, (long) -beforeEndSeconds))
            .endTime(DateUtils.addSeconds(now, (long) -beforeStartSeconds))
            .maxErrorCount(easyEventCommonProperties.getMaxRetryCount());
        eventCompensateService.compensate(param);
    }

    @Override
    public void afterPropertiesSet() {
        Integer schedulePeriod = easyEventCommonProperties.getCompensate().getGlobal().getSchedulePeriod();
        long initDelayMillis = (long) (ThreadLocalRandom.current().nextDouble() * schedulePeriod * 1000L);
        selfScheduledThreadPool
            .scheduleWithFixedDelay(this::doGlobalProcess, initDelayMillis, schedulePeriod * 1000L,
                TimeUnit.MILLISECONDS);
    }
}
