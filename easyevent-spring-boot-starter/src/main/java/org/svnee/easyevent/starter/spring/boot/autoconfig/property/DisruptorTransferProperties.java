package org.svnee.easyevent.starter.spring.boot.autoconfig.property;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.svnee.easyevent.transfer.disruptor.DisruptorTriggerEventSender;

/**
 * Disruptor Transfer Properties
 *
 * @author svnee
 **/
@Slf4j
@ConditionalOnClass(DisruptorTriggerEventSender.class)
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

    public DisruptorTransferSenderProperty getSender() {
        return sender;
    }

    public DisruptorTransferConsumerProperty getConsumer() {
        return consumer;
    }

    /**
     * sender-property
     */
    public static class DisruptorTransferSenderProperty {

        /**
         * disruptor-thread-group
         */
        private String threadGroup = "easyevent-disruptor";

        /**
         * disruptor-thread-prefix
         */
        private String threadPrefix = "disruptor-thread-";

        public String getThreadGroup() {
            return threadGroup;
        }

        public void setThreadGroup(String threadGroup) {
            this.threadGroup = threadGroup;
        }

        public String getThreadPrefix() {
            return threadPrefix;
        }

        public void setThreadPrefix(String threadPrefix) {
            this.threadPrefix = threadPrefix;
        }
    }

    /**
     * consumer property
     */
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

        public String getThreadPrefix() {
            return threadPrefix;
        }

        public void setThreadPrefix(String threadPrefix) {
            this.threadPrefix = threadPrefix;
        }

        public int getCorePoolSize() {
            return corePoolSize;
        }

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public int getMaximumPoolSize() {
            return maximumPoolSize;
        }

        public void setMaximumPoolSize(int maximumPoolSize) {
            this.maximumPoolSize = maximumPoolSize;
        }

        public long getKeepAliveTime() {
            return keepAliveTime;
        }

        public void setKeepAliveTime(long keepAliveTime) {
            this.keepAliveTime = keepAliveTime;
        }

        public int getBufferSize() {
            return bufferSize;
        }

        public void setBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
        }
    }

}
