package com.openquartz.easyevent.example.identify;

import org.springframework.stereotype.Component;
import com.openquartz.easyevent.storage.identify.IdGenerator;

/**
 * @author svnee
 **/
@Component
public class LocalIdGenerator implements IdGenerator {

    @Override
    public <T> Long generateId(T event) {
        return nextId();
    }

    private long lastTimestamp = -1L;
    private long sequence = 0L;

    /**
     * 生成下一个ID
     */
    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨");
        }

        long sequenceBits = 12L;
        if (timestamp == lastTimestamp) {
            // 最大值
            long maxSequence = ~(-1L << sequenceBits);
            sequence = (sequence + 1) & maxSequence;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;

        // 每一部分占用的位数
        long nodeIdBits = 10L;
        long timestampLeftShift = sequenceBits + nodeIdBits;
        long nodeId = 1 << sequenceBits;
        return (timestamp << timestampLeftShift)
                | nodeId
                | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

}
