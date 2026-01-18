package com.openquartz.easyevent.common.concurrent;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TraceContextTest {

    @After
    public void tearDown() {
        TraceContext.clear();
    }

    @Test
    public void testPutAndGet() {
        Map<String, String> traceMap = new HashMap<>();
        traceMap.put("traceId", "12345");
        TraceContext.putTrace(traceMap);

        Assert.assertEquals("12345", TraceContext.getTraceId());
        
        Map<String, String> retrieved = TraceContext.getTrace();
        Assert.assertNotNull(retrieved);
        Assert.assertEquals("12345", retrieved.get("traceId"));
    }

    @Test
    public void testSourceEventId() {
        Long sourceId = 100L;
        // This calls putIfAbsent internally, which had the bug
        TraceContext.putSourceEventIdIfAbsent(sourceId);
        
        Assert.assertEquals(sourceId, TraceContext.getSourceEventId());
        
        // Test putIfAbsent doesn't overwrite
        TraceContext.putSourceEventIdIfAbsent(200L);
        Assert.assertEquals(sourceId, TraceContext.getSourceEventId());
        
        TraceContext.clearSourceEventId();
        Assert.assertNull(TraceContext.getSourceEventId());
    }
    
    @Test
    public void testPersistence() {
        // Verify that the map persists across multiple calls
        TraceContext.putSourceEventIdIfAbsent(1L);
        Map<String, String> traceMap = new HashMap<>();
        traceMap.put("key", "value");
        TraceContext.putTrace(traceMap);
        
        Assert.assertEquals(Long.valueOf(1L), TraceContext.getSourceEventId());
        Assert.assertEquals("value", TraceContext.getTrace().get("key"));
    }
}
