package com.openquartz.easyevent.storage.jdbc.identify;

import com.openquartz.easyevent.common.utils.IpUtil;
import com.openquartz.easyevent.storage.identify.IdGenerator;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Snowflake IdGenerator
 *
 * @author svnee
 */
public class SnowflakeIdGenerator implements IdGenerator {

    private final long workerId;
    private final long datacenterId;
    private long sequence = 0L;

    private final long twepoch = 1288834974657L;
    private final long workerIdBits = 5L;
    private final long datacenterIdBits = 5L;
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);
    private final long sequenceBits = 12L;

    private final long workerIdShift = sequenceBits;
    private final long datacenterIdShift = sequenceBits + workerIdBits;
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator() {
        this(getWorkId(), getDataCenterId());
    }

    public SnowflakeIdGenerator(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    @Override
    public synchronized <T> Long generateId(T event) {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - twepoch) << timestampLeftShift) | (datacenterId << datacenterIdShift) | (workerId << workerIdShift) | sequence;
    }

    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    protected long timeGen() {
        return System.currentTimeMillis();
    }

    private static Long getWorkId() {
        try {
            String hostAddress = IpUtil.getIp();
            int[] ints = toCodePoints(hostAddress);
            int sums = 0;
            for (int b : ints) {
                sums += b;
            }
            return (long) (sums % 32);
        } catch (Exception e) {
            // ignore
            return ThreadLocalRandom.current().nextLong(0, 31);
        }
    }

    private static Long getDataCenterId() {
        try {
            String hostName = java.net.InetAddress.getLocalHost().getHostName();
            int[] ints = toCodePoints(hostName);
            int sums = 0;
            for (int i : ints) {
                sums += i;
            }
            return (long) (sums % 32);
        } catch (Exception e) {
             return ThreadLocalRandom.current().nextLong(0, 31);
        }
    }

    private static int[] toCodePoints(CharSequence str) {
        if (str == null) {
            return new int[0];
        } else if (str.length() == 0) {
            return new int[0];
        } else {
            String s = str.toString();
            int[] result = new int[s.codePointCount(0, s.length())];
            int index = 0;

            for(int i = 0; i < result.length; ++i) {
                result[i] = s.codePointAt(index);
                index += Character.charCount(result[i]);
            }

            return result;
        }
    }
}
