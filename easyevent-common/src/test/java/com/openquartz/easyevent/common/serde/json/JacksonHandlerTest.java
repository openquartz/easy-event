package com.openquartz.easyevent.common.serde.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.Test;

public class JacksonHandlerTest {

    @Test
    public void newMapper_shouldUseThreadSafeStdDateFormat() throws Exception {
        JacksonHandler handler = new JacksonHandler();
        Field mapperField = JacksonHandler.class.getDeclaredField("mapper");
        mapperField.setAccessible(true);
        com.fasterxml.jackson.databind.ObjectMapper mapper =
            (com.fasterxml.jackson.databind.ObjectMapper) mapperField.get(handler);
        DateFormat dateFormat = mapper.getDateFormat();
        assertTrue(dateFormat instanceof StdDateFormat);
    }

    @Test
    public void concurrentSerializeDate_shouldBeStable() throws Exception {
        JacksonHandler handler = new JacksonHandler();
        Date date = new Date(0L);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        List<Callable<String>> tasks = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            tasks.add(() -> handler.toJson(date));
        }
        List<Future<String>> futures = pool.invokeAll(tasks);
        pool.shutdown();

        String first = futures.get(0).get();
        for (Future<String> future : futures) {
            assertEquals(first, future.get());
        }
    }
}
