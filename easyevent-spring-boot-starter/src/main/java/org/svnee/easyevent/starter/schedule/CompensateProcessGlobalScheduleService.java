package org.svnee.easyevent.starter.schedule;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.InitializingBean;
import org.svnee.easyevent.common.concurrent.ThreadFactoryBuilder;
import org.svnee.easyevent.common.utils.DateUtils;
import org.svnee.easyevent.core.compensate.EventCompensateParam;
import org.svnee.easyevent.core.compensate.EventCompensateService;
import org.svnee.easyevent.starter.spring.boot.autoconfig.property.EasyEventCommonProperties;
import org.svnee.easyevent.starter.spring.boot.autoconfig.property.EasyEventCommonProperties.EventCompensateProcessProperty;

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
        EventCompensateParam param = EventCompensateParam.builder()
            .compensateState(easyEventCommonProperties.getCompensate().getGlobal().getCompensateState())
            .offset(easyEventCommonProperties.getCompensate().getGlobal().getOffset())
            .startTime(DateUtils.addSeconds(new Date(),
                (long) -easyEventCommonProperties.getCompensate().getGlobal().getBeforeStartSeconds()))
            .endTime(DateUtils
                .addSeconds(now, (long) -easyEventCommonProperties.getCompensate().getGlobal().getBeforeEndSeconds()))
            .maxErrorCount(easyEventCommonProperties.getMaxRetryCount());
        eventCompensateService.compensate(param);
    }

    @Override
    public void afterPropertiesSet() {
        Integer schedulePeriod = easyEventCommonProperties.getCompensate().getGlobal().getSchedulePeriod();
        double initDelaySeconds = new Random(1).nextDouble() * schedulePeriod;
        selfScheduledThreadPool
            .scheduleAtFixedRate(this::doGlobalProcess, (int) initDelaySeconds, schedulePeriod, TimeUnit.SECONDS);
    }
}
