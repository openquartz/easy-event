package com.openquartz.easyevent.starter.disruptor.spring.boot.autoconfig;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Disruptor Transfer Properties
 *
 * @author svnee
 **/
@Getter
@Slf4j
@ConfigurationProperties(prefix = DisruptorTransferProperties.PREFIX)
public class DisruptorTransferProperties {

    public static final String PREFIX = "easyevent.transfer.trigger.disruptor";

    /**
     * sender property
     */
    private final DisruptorTransferSenderProperty sender = new DisruptorTransferSenderProperty();

    /**
     * consumer property
     */
    private final DisruptorTransferConsumerProperty consumer = new DisruptorTransferConsumerProperty();

    /**
     * sender-property
     */
    @Setter
    @Getter
    public static class DisruptorTransferSenderProperty {

        /**
         * disruptor-thread-group
         */
        private String threadGroup = "easyevent-disruptor";

        /**
         * disruptor-thread-prefix
         */
        private String threadPrefix = "disruptor-thread-";

    }

    /**
     * consumer property
     */
    @Setter
    @Getter
    public static class DisruptorTransferConsumerProperty {

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

        @Override
        public String toString() {
            return "DisruptorTransferConsumerProperty{" +
                "threadPrefix='" + threadPrefix + '\'' +
                ", corePoolSize=" + corePoolSize +
                ", maximumPoolSize=" + maximumPoolSize +
                ", keepAliveTime=" + keepAliveTime +
                ", bufferSize=" + bufferSize +
                '}';
        }
    }

    @Override
    public String toString() {
        return "DisruptorTransferProperties{" +
            "sender=" + sender +
            ", consumer=" + consumer +
            '}';
    }
}
