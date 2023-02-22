package org.svnee.easyevent.starter.schedule;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.InitializingBean;
import org.svnee.easyevent.common.concurrent.ThreadFactoryBuilder;
import org.svnee.easyevent.common.utils.DateUtils;
import org.svnee.easyevent.common.utils.IpUtil;
import org.svnee.easyevent.core.compensate.EventCompensateParam;
import org.svnee.easyevent.core.compensate.EventCompensateService;
import org.svnee.easyevent.starter.spring.boot.autoconfig.property.EasyEventCommonProperties;
import org.svnee.easyevent.starter.spring.boot.autoconfig.property.EasyEventCommonProperties.EventCompensateProcessProperty;

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
        EventCompensateParam param = EventCompensateParam.builder()
            .compensateState(easyEventCommonProperties.getCompensate().getSelf().getCompensateState())
            .offset(easyEventCommonProperties.getCompensate().getSelf().getOffset())
            .startTime(DateUtils.addSeconds(new Date(),
                (long) -easyEventCommonProperties.getCompensate().getSelf().getBeforeStartSeconds()))
            .endTime(DateUtils
                .addSeconds(now, (long) -easyEventCommonProperties.getCompensate().getSelf().getBeforeEndSeconds()))
            .maxErrorCount(easyEventCommonProperties.getMaxRetryCount())
            .ipAddr(IpUtil.getIp());
        eventCompensateService.compensate(param);
    }

    @Override
    public void afterPropertiesSet() {
        Integer schedulePeriod = easyEventCommonProperties.getCompensate().getSelf().getSchedulePeriod();
        double initDelaySeconds = new Random(1).nextDouble() * schedulePeriod;
        selfScheduledThreadPool
            .scheduleAtFixedRate(this::doSelfProcess, (int) initDelaySeconds, schedulePeriod, TimeUnit.SECONDS);
    }
}
