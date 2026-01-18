package com.openquartz.easyevent.storage.jdbc.identify;

import com.openquartz.easyevent.storage.identify.IdGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SnowflakeIdGeneratorTest {

    @Test
    public void testGenerateId() {
        IdGenerator idGenerator = new SnowflakeIdGenerator(1, 1);
        long id1 = idGenerator.generateId(null);
        long id2 = idGenerator.generateId(null);

        Assert.assertTrue(id1 > 0);
        Assert.assertTrue(id2 > 0);
        Assert.assertTrue(id2 > id1);
    }

    @Test
    public void testUniqueId() throws InterruptedException {
        int threadCount = 10;
        int idCountPerThread = 1000;
        IdGenerator idGenerator = new SnowflakeIdGenerator(1, 1);
        Set<Long> ids = ConcurrentHashMap.newKeySet();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < idCountPerThread; j++) {
                        ids.add(idGenerator.generateId(null));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Assert.assertEquals(threadCount * idCountPerThread, ids.size());
    }
}
