package com.openquartz.easyevent.starter.schedule;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

public class CompensateProcessScheduleServiceTest {

    @Test
    public void globalSchedule_shouldNotUseFixedSeedRandom() throws Exception {
        Path path = Paths.get("src/main/java/com/openquartz/easyevent/starter/schedule/CompensateProcessGlobalScheduleService.java");
        String source = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        assertFalse(source.contains("new Random(1)"));
        assertTrue(source.contains("new Random()"));
    }

    @Test
    public void selfSchedule_shouldNotUseFixedSeedRandom() throws Exception {
        Path path = Paths.get("src/main/java/com/openquartz/easyevent/starter/schedule/CompensateProcessSelfScheduleService.java");
        String source = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        assertFalse(source.contains("new Random(1)"));
        assertTrue(source.contains("new Random()"));
    }

    @Test
    public void scheduleServices_shouldExposeAfterPropertiesSet() throws Exception {
        Method global = CompensateProcessGlobalScheduleService.class.getMethod("afterPropertiesSet");
        Method self = CompensateProcessSelfScheduleService.class.getMethod("afterPropertiesSet");
        assertNotNull(global);
        assertNotNull(self);
    }
}
