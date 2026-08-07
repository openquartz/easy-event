package com.openquartz.easyevent.starter.soa.rocketmq;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class SoaRocketMqEventTriggerTest {

    @Test
    public void toDelayLevel_shouldMapSecondsToRocketMqLevels() {
        assertEquals(1, SoaRocketMqEventTrigger.toDelayLevel(null));
        assertEquals(1, SoaRocketMqEventTrigger.toDelayLevel(1));
        assertEquals(3, SoaRocketMqEventTrigger.toDelayLevel(10));
        assertEquals(18, SoaRocketMqEventTrigger.toDelayLevel(7200));
        assertEquals(18, SoaRocketMqEventTrigger.toDelayLevel(7201));
    }
}
