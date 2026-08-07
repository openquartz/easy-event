package com.openquartz.easyevent.transfer.rocket;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RocketMqEventTriggerTest {

    @Test
    public void toDelayLevel_shouldMapSecondsToRocketMqLevels() {
        assertEquals(1, RocketMqEventTrigger.toDelayLevel(null));
        assertEquals(1, RocketMqEventTrigger.toDelayLevel(0));
        assertEquals(1, RocketMqEventTrigger.toDelayLevel(1));
        assertEquals(2, RocketMqEventTrigger.toDelayLevel(5));
        assertEquals(3, RocketMqEventTrigger.toDelayLevel(10));
        assertEquals(5, RocketMqEventTrigger.toDelayLevel(60));
        assertEquals(18, RocketMqEventTrigger.toDelayLevel(10_000));
    }
}
