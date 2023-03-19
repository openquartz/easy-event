package org.svnee.easyevent.starter.spring.boot.autoconfig.property;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.svnee.easyevent.common.constant.CommonConstants;
import org.svnee.easyevent.common.property.EasyEventProperties;
import org.svnee.easyevent.common.utils.StringUtils;
import org.svnee.easyevent.storage.model.EventLifecycleState;

/**
 * EasyEventCommonProperties
 *
 * @author svnee
 **/
@Slf4j
@ConfigurationProperties(prefix = EasyEventCommonProperties.PREFIX)
public class EasyEventCommonProperties implements EasyEventProperties {

    public static final String PREFIX = "easyevent.common";

    /**
     * App-ID
     */
    private String appId;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount = 10;

    /**
     * 补偿服务配置
     */
    private EventCompensateProperty compensate = new EventCompensateProperty();

    /**
     * event notify
     */
    private EventNotifyProperty notify = new EventNotifyProperty();

    @Override
    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    @Override
    public Integer getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(Integer maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public EventCompensateProperty getCompensate() {
        return compensate;
    }

    public void setCompensate(
        EventCompensateProperty compensate) {
        this.compensate = compensate;
    }

    public EventNotifyProperty getNotify() {
        return notify;
    }

    public void setNotify(
        EventNotifyProperty notify) {
        this.notify = notify;
    }

    /**
     * compensate property
     *
     * @author svnee
     */
    public static class EventCompensateProperty {

        private EventCompensateThreadPoolProperty threadPool = new EventCompensateThreadPoolProperty();
        private EventCompensateProcessProperty self = new EventCompensateProcessProperty(10, 60);
        private EventCompensateProcessProperty global = new EventCompensateProcessProperty(60, 3600);

        public EventCompensateProcessProperty getSelf() {
            return self;
        }

        public void setSelf(
            EventCompensateProcessProperty self) {
            this.self = self;
        }

        public EventCompensateProcessProperty getGlobal() {
            return global;
        }

        public void setGlobal(EventCompensateProcessProperty global) {
            this.global = global;
        }

        public EventCompensateThreadPoolProperty getThreadPool() {
            return threadPool;
        }

        public void setThreadPool(
            EventCompensateThreadPoolProperty threadPool) {
            this.threadPool = threadPool;
        }
    }

    public static class EventNotifyProperty {

        /**
         * enable notify
         */
        private boolean enabled = true;

        /**
         * 唯一标识
         */
        private String identify = "EventFailedNotifier";

        /**
         * 线程名前缀
         */
        private String threadPrefix = "EventNotifierThread";

        /**
         * 周期
         * 单位：minutes
         */
        private int period = 10;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getThreadPrefix() {
            return threadPrefix;
        }

        public void setThreadPrefix(String threadPrefix) {
            this.threadPrefix = threadPrefix;
        }

        public String getIdentify() {
            return identify;
        }

        public void setIdentify(String identify) {
            this.identify = identify;
        }

        public int getPeriod() {
            return period;
        }

        public void setPeriod(int period) {
            this.period = period;
        }
    }

    public static class EventCompensateThreadPoolProperty {

        /**
         * 线程名前缀
         */
        private String threadPrefix = "EventCompensateThread";

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
            return "EventCompensateThreadPoolProperty{" +
                "threadPrefix='" + threadPrefix + '\'' +
                ", corePoolSize=" + corePoolSize +
                ", maximumPoolSize=" + maximumPoolSize +
                ", keepAliveTime=" + keepAliveTime +
                ", maxBlockingQueueSize=" + maxBlockingQueueSize +
                '}';
        }
    }

    /**
     * compensate process property
     *
     * @author svnee
     */
    public static class EventCompensateProcessProperty {

        private boolean enabled = true;

        private String threadPoolThreadPrefix = "EventCompensate";
        private Integer threadPoolCoreSize = 1;

        private String compensateState = "AVAILABLE,TRANSFER_FAILED,PROCESS_FAILED";
        private Integer beforeStartSeconds;
        private Integer beforeEndSeconds;
        private Integer offset = 100;
        private Integer schedulePeriod = 10;

        public EventCompensateProcessProperty() {
        }

        public EventCompensateProcessProperty(Integer beforeStartSeconds, Integer beforeEndSeconds) {
            this.beforeStartSeconds = beforeStartSeconds;
            this.beforeEndSeconds = beforeEndSeconds;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Integer getBeforeStartSeconds() {
            return beforeStartSeconds;
        }

        public void setBeforeStartSeconds(Integer beforeStartSeconds) {
            this.beforeStartSeconds = beforeStartSeconds;
        }

        public Integer getBeforeEndSeconds() {
            return beforeEndSeconds;
        }

        public void setBeforeEndSeconds(Integer beforeEndSeconds) {
            this.beforeEndSeconds = beforeEndSeconds;
        }

        public Integer getOffset() {
            return offset;
        }

        public void setOffset(Integer offset) {
            this.offset = offset;
        }

        public Integer getThreadPoolCoreSize() {
            return threadPoolCoreSize;
        }

        public String getThreadPoolThreadPrefix() {
            return threadPoolThreadPrefix;
        }

        public void setThreadPoolThreadPrefix(String threadPoolThreadPrefix) {
            this.threadPoolThreadPrefix = threadPoolThreadPrefix;
        }

        public void setThreadPoolCoreSize(Integer threadPoolCoreSize) {
            this.threadPoolCoreSize = threadPoolCoreSize;
        }

        public List<EventLifecycleState> getCompensateState() {
            if (StringUtils.isBlank(compensateState)) {
                return Collections.emptyList();
            }
            return Stream.of(compensateState.split(CommonConstants.COMMA))
                .map(String::trim)
                .map(EventLifecycleState::of)
                .collect(Collectors.toList());
        }

        public void setCompensateState(String compensateState) {
            this.compensateState = compensateState;
        }

        public Integer getSchedulePeriod() {
            return schedulePeriod;
        }

        public void setSchedulePeriod(Integer schedulePeriod) {
            this.schedulePeriod = schedulePeriod;
        }
    }
}
