package com.openquartz.easyevent.starter.schedule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyevent.core.compensate.EventCompensateParam;
import com.openquartz.easyevent.core.compensate.EventCompensateService;
import com.openquartz.easyevent.starter.spring.boot.autoconfig.property.EasyEventCommonProperties;
import com.openquartz.easyevent.starter.spring.boot.autoconfig.property.EasyEventCommonProperties.EventCompensateProcessProperty;
import com.openquartz.easyevent.storage.model.EventLifecycleState;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class CompensateProcessScheduleServiceTest {

    private static final RejectedExecutionHandler REJECT_HANDLER = (r, e) -> {
    };

    @Test
    public void globalSchedule_shouldUseThreadLocalRandomAndFixedDelay() throws Exception {
        Path path = Paths.get("src/main/java/com/openquartz/easyevent/starter/schedule/CompensateProcessGlobalScheduleService.java");
        String source = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        assertFalse(source.contains("new Random(1)"));
        assertFalse(source.contains("new Random()"));
        assertTrue(source.contains("ThreadLocalRandom"));
        assertTrue(source.contains("scheduleWithFixedDelay"));
        assertTrue(source.contains("(long) -beforeEndSeconds"));
        assertTrue(source.contains("(long) -beforeStartSeconds"));
    }

    @Test
    public void selfSchedule_shouldUseThreadLocalRandomAndFixedDelay() throws Exception {
        Path path = Paths.get("src/main/java/com/openquartz/easyevent/starter/schedule/CompensateProcessSelfScheduleService.java");
        String source = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        assertFalse(source.contains("new Random(1)"));
        assertFalse(source.contains("new Random()"));
        assertTrue(source.contains("ThreadLocalRandom"));
        assertTrue(source.contains("scheduleWithFixedDelay"));
        assertTrue(source.contains("(long) -beforeEndSeconds"));
        assertTrue(source.contains("(long) -beforeStartSeconds"));
    }

    @Test
    public void scheduleServices_shouldExposeAfterPropertiesSet() throws Exception {
        Method global = CompensateProcessGlobalScheduleService.class.getMethod("afterPropertiesSet");
        Method self = CompensateProcessSelfScheduleService.class.getMethod("afterPropertiesSet");
        assertNotNull(global);
        assertNotNull(self);
    }

    @Test
    public void doSelfProcess_shouldBuildParamWithIpAndTimeWindow() throws Exception {
        EventCompensateService compensateService = mock(EventCompensateService.class);
        EasyEventCommonProperties properties = buildProperties(30, 120, 5, "self-app");

        CompensateProcessSelfScheduleService service =
            new CompensateProcessSelfScheduleService(compensateService, properties, REJECT_HANDLER);

        long beforeInvoke = System.currentTimeMillis();
        invokePrivate(service, "doSelfProcess");
        long afterInvoke = System.currentTimeMillis();

        ArgumentCaptor<EventCompensateParam> captor = ArgumentCaptor.forClass(EventCompensateParam.class);
        verify(compensateService).compensate(captor.capture());
        EventCompensateParam param = captor.getValue();

        assertEqualsApprox(beforeInvoke - 120_000L, param.getStartTime().getTime(), 2000L);
        assertEqualsApprox(beforeInvoke - 30_000L, param.getEndTime().getTime(), 2000L);
        assertTrue(afterInvoke - param.getEndTime().getTime() >= 0);
        assertEquals(5, param.getMaxErrorCount());
        assertEquals("self-app", param.getAppId());
        assertTrue(param.getCompensateState().contains(EventLifecycleState.TRANSFER_SUCCESS));
        assertNotNull(param.getIpAddr());
    }

    @Test
    public void doGlobalProcess_shouldBuildParamWithoutIpAndTimeWindow() throws Exception {
        EventCompensateService compensateService = mock(EventCompensateService.class);
        EasyEventCommonProperties properties = buildProperties(60, 3600, 7, "global-app");
        properties.getCompensate().setGlobal(new EventCompensateProcessProperty(90, 1800));

        CompensateProcessGlobalScheduleService service =
            new CompensateProcessGlobalScheduleService(compensateService, properties, REJECT_HANDLER);

        long beforeInvoke = System.currentTimeMillis();
        invokePrivate(service, "doGlobalProcess");

        ArgumentCaptor<EventCompensateParam> captor = ArgumentCaptor.forClass(EventCompensateParam.class);
        verify(compensateService).compensate(captor.capture());
        EventCompensateParam param = captor.getValue();

        assertEqualsApprox(beforeInvoke - 1800_000L, param.getStartTime().getTime(), 2000L);
        assertEqualsApprox(beforeInvoke - 90_000L, param.getEndTime().getTime(), 2000L);
        assertEquals(7, param.getMaxErrorCount());
        assertEquals("global-app", param.getAppId());
        assertTrue(param.getCompensateState().contains(EventLifecycleState.TRANSFER_SUCCESS));
        assertNull(param.getIpAddr());
    }

    @Test
    public void selfAfterPropertiesSet_shouldScheduleWithFixedDelayOnly() throws Exception {
        EventCompensateService compensateService = mock(EventCompensateService.class);
        EasyEventCommonProperties properties = buildProperties(10, 60, 10, "app");
        properties.getCompensate().getSelf().setSchedulePeriod(12);

        CompensateProcessSelfScheduleService service =
            new CompensateProcessSelfScheduleService(compensateService, properties, REJECT_HANDLER);

        ScheduledThreadPoolExecutor executor = mock(ScheduledThreadPoolExecutor.class);
        replaceExecutor(service, "selfScheduledThreadPool", executor);

        service.afterPropertiesSet();

        long periodMillis = 12_000L;
        ArgumentCaptor<Long> initDelayCaptor = ArgumentCaptor.forClass(Long.class);
        verify(executor).scheduleWithFixedDelay(any(Runnable.class), initDelayCaptor.capture(),
            eq(periodMillis), eq(TimeUnit.MILLISECONDS));
        long initDelay = initDelayCaptor.getValue();
        assertTrue(initDelay >= 0 && initDelay < periodMillis);
        verify(executor, never()).scheduleAtFixedRate(any(), anyLong(), anyLong(), any());
    }

    @Test
    public void globalAfterPropertiesSet_shouldScheduleWithFixedDelayOnly() throws Exception {
        EventCompensateService compensateService = mock(EventCompensateService.class);
        EasyEventCommonProperties properties = buildProperties(10, 60, 10, "app");
        properties.getCompensate().getGlobal().setSchedulePeriod(15);

        CompensateProcessGlobalScheduleService service =
            new CompensateProcessGlobalScheduleService(compensateService, properties, REJECT_HANDLER);

        ScheduledThreadPoolExecutor executor = mock(ScheduledThreadPoolExecutor.class);
        replaceExecutor(service, "selfScheduledThreadPool", executor);

        service.afterPropertiesSet();

        long periodMillis = 15_000L;
        ArgumentCaptor<Long> initDelayCaptor = ArgumentCaptor.forClass(Long.class);
        verify(executor).scheduleWithFixedDelay(any(Runnable.class), initDelayCaptor.capture(),
            eq(periodMillis), eq(TimeUnit.MILLISECONDS));
        long initDelay = initDelayCaptor.getValue();
        assertTrue(initDelay >= 0 && initDelay < periodMillis);
        verify(executor, never()).scheduleAtFixedRate(any(), anyLong(), anyLong(), any());
    }

    private static EasyEventCommonProperties buildProperties(int beforeStart, int beforeEnd, int maxRetry,
        String appId) {
        EasyEventCommonProperties properties = new EasyEventCommonProperties();
        properties.setAppId(appId);
        properties.setMaxRetryCount(maxRetry);
        EventCompensateProcessProperty self = new EventCompensateProcessProperty(beforeStart, beforeEnd);
        properties.getCompensate().setSelf(self);
        return properties;
    }

    private static void invokePrivate(Object target, String methodName) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(target);
    }

    private static void replaceExecutor(Object target, String fieldName, ScheduledThreadPoolExecutor executor)
        throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, executor);
    }

    private static void assertEqualsApprox(long expected, long actual, long toleranceMs) {
        assertTrue(Math.abs(expected - actual) <= toleranceMs,
            "expected ~" + expected + " but was " + actual);
    }
}
