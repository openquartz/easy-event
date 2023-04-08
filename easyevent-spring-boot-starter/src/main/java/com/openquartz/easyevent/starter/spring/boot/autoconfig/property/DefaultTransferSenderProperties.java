package com.openquartz.easyevent.starter.spring.boot.autoconfig.property;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DefaultTransferSenderProperties
 *
 * sender properties
 *
 * @author svnee
 **/
@Getter
@Setter
@Slf4j
@ConfigurationProperties(prefix = DefaultTransferSenderProperties.PREFIX)
public class DefaultTransferSenderProperties {

    public static final String PREFIX = "easyevent.transfer.sender";

    /**
     * thread-pool
     */
    private final DefaultTransferSenderThreadPoolProperties threadPool = new DefaultTransferSenderThreadPoolProperties();

    /**
     * thread-pool properties
     */
    public static class DefaultTransferSenderThreadPoolProperties {

        /**
         * 线程名前缀
         */
        private String threadPrefix = "DefaultTransferThreadPool";

        /**
         * 核心线程数
         * core-pool-size
         */
        private Integer corePoolSize = 10;

        /**
         * 最大线程池数
         * maximum-pool-size
         */
        private Integer maximumPoolSize = 20;

        /**
         * 单位：秒
         * keep-alive-time
         */
        private Long keepAliveTime = 30L;

        /**
         * 阻塞队列最大长度
         * max-blocking-queue-size
         */
        private Integer maxBlockingQueueSize = 2048;

        public String getThreadPrefix() {
            return threadPrefix;
        }

        public void setThreadPrefix(String threadPrefix) {
            this.threadPrefix = threadPrefix;
        }

        public Integer getCorePoolSize() {
            return corePoolSize;
        }

        public void setCorePoolSize(Integer corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public Integer getMaximumPoolSize() {
            return maximumPoolSize;
        }

        public void setMaximumPoolSize(Integer maximumPoolSize) {
            this.maximumPoolSize = maximumPoolSize;
        }

        public Long getKeepAliveTime() {
            return keepAliveTime;
        }

        public void setKeepAliveTime(Long keepAliveTime) {
            this.keepAliveTime = keepAliveTime;
        }

        public Integer getMaxBlockingQueueSize() {
            return maxBlockingQueueSize;
        }

        public void setMaxBlockingQueueSize(Integer maxBlockingQueueSize) {
            this.maxBlockingQueueSize = maxBlockingQueueSize;
        }

        @Override
        public String toString() {
            return "DefaultTransferSenderThreadPoolProperties{" +
                "corePoolSize=" + corePoolSize +
                ", maximumPoolSize=" + maximumPoolSize +
                ", keepAliveTime=" + keepAliveTime +
                ", maxBlockingQueueSize=" + maxBlockingQueueSize +
                '}';
        }
    }

    @Override
    public String toString() {
        return "DefaultTransferSenderProperties{" +
            "threadPool=" + threadPool +
            '}';
    }
}
