package com.openquartz.easyevent.starter.schedule;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.InitializingBean;
import com.openquartz.easyevent.common.concurrent.ThreadFactoryBuilder;
import com.openquartz.easyevent.common.concurrent.lock.LockBizType;
import com.openquartz.easyevent.common.concurrent.lock.LockSupport;
import com.openquartz.easyevent.common.model.Pair;
import com.openquartz.easyevent.core.notify.EventHandleFailedNotifierAdapter;
import com.openquartz.easyevent.core.notify.EventNotifier;
import com.openquartz.easyevent.starter.spring.boot.autoconfig.property.EasyEventCommonProperties.EventNotifyProperty;
import com.openquartz.easyevent.storage.api.EventStorageService;

/**
 * DefaultEventHandleFailedNotifier
 * schedule notify handle failed event by your {@link EventNotifier}
 *
 * @author svnee
 **/
public class DefaultEventHandleFailedNotifier extends EventHandleFailedNotifierAdapter implements InitializingBean {

    private final LockSupport lockSupport;
    private final EventNotifyProperty eventNotifyProperty;
    private final ScheduledExecutorService notifyScheduledThreadPool;

    public DefaultEventHandleFailedNotifier(EventStorageService eventStorageService,
        Integer maxErrorCount,
        EventNotifier notifier,
        EventNotifyProperty eventNotifyProperty,
        LockSupport lockSupport) {

        super(eventStorageService, maxErrorCount, notifier);

        this.lockSupport = lockSupport;
        this.eventNotifyProperty = eventNotifyProperty;

        ThreadFactory factory = new ThreadFactoryBuilder()
            .setNameFormat(eventNotifyProperty.getThreadPrefix() + "-thread-%d")
            .build();
        this.notifyScheduledThreadPool = Executors.newScheduledThreadPool(1, factory);
    }

    @Override
    public void handle() {
        lockSupport
            .consumeIfTryLock(Pair.of(eventNotifyProperty.getIdentify(), LockBizType.EVENT_HANDLE_FAIL_NOTIFIER),
                super::handle);
    }

    @Override
    public void afterPropertiesSet() {
        int minute = LocalDateTime.now(ZoneId.systemDefault()).getMinute();
        int delay = (minute / eventNotifyProperty.getPeriod() + 1) * eventNotifyProperty.getPeriod() - minute;
        notifyScheduledThreadPool
            .scheduleAtFixedRate(this::handle, delay, eventNotifyProperty.getPeriod(), TimeUnit.MINUTES);
    }
}
