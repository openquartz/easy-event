package com.openquartz.easyevent.transfer.kafka;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.openquartz.easyevent.common.constant.CommonConstants;
import com.openquartz.easyevent.transfer.kafka.common.KafkaTriggerProperty;
import com.openquartz.easyevent.transfer.kafka.common.KafkaTriggerProperty.KafkaConsumerProperty;
import com.openquartz.easyevent.transfer.kafka.property.KafkaCommonProperty;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.junit.Test;

public class KafkaEventTriggerLatchTest {

    @Test
    public void constructor_shouldCountCurrencyThreadsAndClampAssignedPartition() throws Exception {
        KafkaTriggerProperty triggerProperty = new KafkaTriggerProperty();
        Map<String, KafkaConsumerProperty> map = new HashMap<>();

        KafkaConsumerProperty subscribe = new KafkaConsumerProperty();
        subscribe.setPartition(CommonConstants.ALL_MATCH_EXPRESSION);
        subscribe.setCurrency(3);
        map.put("c1", subscribe);

        KafkaConsumerProperty assign = new KafkaConsumerProperty();
        assign.setPartition("0");
        assign.setCurrency(4);
        map.put("c2", assign);

        triggerProperty.setConsumerPropertyMap(map);

        KafkaEventTrigger trigger = new KafkaEventTrigger(
            triggerProperty,
            new KafkaCommonProperty(),
            msg -> {
            },
            null,
            null
        );

        Field field = KafkaEventTrigger.class.getDeclaredField("countDownLatch");
        field.setAccessible(true);
        CountDownLatch latch = (CountDownLatch) field.get(trigger);
        // subscribe currency(3) + assign clamped to 1
        assertEquals(4, latch.getCount());
    }

    @Test
    public void consumerConfig_shouldResetToEarliest() throws Exception {
        String source = new String(Files.readAllBytes(
            Paths.get("src/main/java/com/openquartz/easyevent/transfer/kafka/KafkaEventTrigger.java")),
            StandardCharsets.UTF_8);
        assertTrue(source.contains("AUTO_OFFSET_RESET_CONFIG, \"earliest\""));
        assertTrue(source.contains("ENABLE_AUTO_COMMIT_CONFIG"));
        assertTrue(source.contains("\"false\""));
    }
}
