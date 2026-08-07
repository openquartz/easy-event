package com.openquartz.easyevent.starter.schedule;

import java.util.Date;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.InitializingBean;
import com.openquartz.easyevent.common.concurrent.ThreadFactoryBuilder;
import com.openquartz.easyevent.common.utils.DateUtils;
import com.openquartz.easyevent.common.utils.IpUtil;
import com.openquartz.easyevent.core.compensate.EventCompensateParam;
import com.openquartz.easyevent.core.compensate.EventCompensateService;
import com.openquartz.easyevent.starter.spring.boot.autoconfig.property.EasyEventCommonProperties;
import com.openquartz.easyevent.starter.spring.boot.autoconfig.property.EasyEventCommonProperties.EventCompensateProcessProperty;

/**
 * self  process service
 *
 * @author svnee
 **/
public class CompensateProcessSelfScheduleService implements InitializingBean {

    private final EventCompensateService eventCompensateService;
    private final EasyEventCommonProperties easyEventCommonProperties;
    private final ScheduledThreadPoolExecutor selfScheduledThreadPool;

    private ScheduledThreadPoolExecutor init(EventCompensateProcessProperty property,
        RejectedExecutionHandler rejectedExecutionHandler) {
        return new ScheduledThreadPoolExecutor(property.getThreadPoolCoreSize(),
            new ThreadFactoryBuilder()
                .setNameFormat(property.getThreadPoolThreadPrefix() + "-self-thread-%d")
                .build(),
            rejectedExecutionHandler);
    }

    public CompensateProcessSelfScheduleService(
        EventCompensateService eventCompensateService,
        EasyEventCommonProperties easyEventCommonProperties,
        RejectedExecutionHandler scheduledCompensateRejectedExecutionHandler) {

        this.eventCompensateService = eventCompensateService;
        this.easyEventCommonProperties = easyEventCommonProperties;
        this.selfScheduledThreadPool = init(easyEventCommonProperties.getCompensate().getSelf(),
            scheduledCompensateRejectedExecutionHandler);
    }

    private void doSelfProcess() {
        Date now = new Date();
        // Window: [now - beforeEndSeconds, now - beforeStartSeconds)
        // e.g. defaults (beforeStart=10, beforeEnd=60) => events aged 10s~60s.
        Integer beforeStartSeconds = easyEventCommonProperties.getCompensate().getSelf().getBeforeStartSeconds();
        Integer beforeEndSeconds = easyEventCommonProperties.getCompensate().getSelf().getBeforeEndSeconds();
        EventCompensateParam param = EventCompensateParam.builder(easyEventCommonProperties.getAppId())
            .compensateState(easyEventCommonProperties.getCompensate().getSelf().getCompensateState())
            .offset(easyEventCommonProperties.getCompensate().getSelf().getOffset())
            .startTime(DateUtils.addSeconds(now, (long) -beforeEndSeconds))
            .endTime(DateUtils.addSeconds(now, (long) -beforeStartSeconds))
            .maxErrorCount(easyEventCommonProperties.getMaxRetryCount())
            .ipAddr(IpUtil.getIp());
        eventCompensateService.compensate(param);
    }

    @Override
    public void afterPropertiesSet() {
        Integer schedulePeriod = easyEventCommonProperties.getCompensate().getSelf().getSchedulePeriod();
        long initDelayMillis = (long) (ThreadLocalRandom.current().nextDouble() * schedulePeriod * 1000L);
        selfScheduledThreadPool
            .scheduleWithFixedDelay(this::doSelfProcess, initDelayMillis, schedulePeriod * 1000L,
                TimeUnit.MILLISECONDS);
    }
}
