package com.openquartz.easyevent.transfer.disruptor.property;

import lombok.Data;

/**
 * RocketMQ consumer Property
 *
 * @author svnee
 **/
@Data
public class DisruptorConsumerProperty {

    /**
     * 线程
     */
    private String threadPrefix = "easyevent-disruptor";

    /**
     * 核心线程池大小
     */
    private int corePoolSize = Runtime.getRuntime().availableProcessors() << 1;

    /**
     * 最大线程池大小
     */
    private int maximumPoolSize = Runtime.getRuntime().availableProcessors() << 1;

    /**
     * 线程保持活跃时间
     */
    private long keepAliveTime = 0;

    /**
     * 触发队列长度
     */
    private int bufferSize = 4096;
}
