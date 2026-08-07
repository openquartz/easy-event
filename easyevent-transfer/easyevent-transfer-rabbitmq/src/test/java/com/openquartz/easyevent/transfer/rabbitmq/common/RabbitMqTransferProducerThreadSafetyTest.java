package com.openquartz.easyevent.transfer.rabbitmq.common;

import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;

public class RabbitMqTransferProducerThreadSafetyTest {

    @Test
    public void producer_shouldSynchronizeChannelAccess() throws Exception {
        String source = new String(Files.readAllBytes(
            Paths.get(
                "src/main/java/com/openquartz/easyevent/transfer/rabbitmq/common/RabbitMqTransferProducer.java")),
            StandardCharsets.UTF_8);
        assertTrue(source.contains("channelLock"));
        assertTrue(source.contains("synchronized (channelLock)"));
    }
}
