package org.svnee.easyevent.starter.spring.boot.autoconfig.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * EventBus Trace TheadPool Properties
 *
 * @author svnee
 **/
@Getter
@Setter
@ConfigurationProperties(prefix = DefaultEventBusProperties.PREFIX)
public class DefaultEventBusProperties {

    public static final String PREFIX = "easyevent.bus";

    /**
     * threadPool
     */
    private final EventBusThreadPoolProperties threadPool = new EventBusThreadPoolProperties();

    /**
     * ThreadPool-Properties
     */
    public static class EventBusThreadPoolProperties {

        /**
         * 线程名前缀
         */
        private String threadPrefix = "DefaultEventBusThreadPool";

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
            return "EventBusThreadPoolProperties{" +
                "threadPrefix='" + threadPrefix + '\'' +
                ", corePoolSize=" + corePoolSize +
                ", maximumPoolSize=" + maximumPoolSize +
                ", keepAliveTime=" + keepAliveTime +
                ", maxBlockingQueueSize=" + maxBlockingQueueSize +
                '}';
        }
    }

    @Override
    public String toString() {
        return "DefaultEventBusProperties{" +
            "threadPool=" + threadPool +
            '}';
    }
}
