package com.openquartz.easyevent.starter.spring.boot.autoconfig.property;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import com.openquartz.easyevent.common.constant.CommonConstants;
import com.openquartz.easyevent.common.property.EasyEventProperties;
import com.openquartz.easyevent.common.utils.StringUtils;
import com.openquartz.easyevent.storage.model.EventLifecycleState;

/**
 * EasyEventCommonProperties
 *
 * @author svnee
 **/
@Setter
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
    @Getter
    private EventCompensateProperty compensate = new EventCompensateProperty();

    /**
     * event notify
     */
    @Getter
    private EventNotifyProperty notify = new EventNotifyProperty();

    @Override
    public String getAppId() {
        return appId;
    }

    @Override
    public Integer getMaxRetryCount() {
        return maxRetryCount;
    }

    /**
     * compensate property
     *
     * @author svnee
     */
    @Data
    public static class EventCompensateProperty {

        private EventCompensateThreadPoolProperty threadPool = new EventCompensateThreadPoolProperty();
        private EventCompensateProcessProperty self = new EventCompensateProcessProperty(10, 60);
        private EventCompensateProcessProperty global = new EventCompensateProcessProperty(60, 3600);

        @Override
        public String toString() {
            return "EventCompensateProperty{" +
                "threadPool=" + threadPool +
                ", self=" + self +
                ", global=" + global +
                '}';
        }
    }

    @Setter
    @Getter
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

    }

    @Setter
    @Getter
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
    @Setter
    public static class EventCompensateProcessProperty {

        @Getter
        private boolean enabled = true;

        @Getter
        private String threadPoolThreadPrefix = "EventCompensate";
        @Getter
        private Integer threadPoolCoreSize = 1;

        private String compensateState = "AVAILABLE,TRANSFER_FAILED,PROCESS_FAILED";
        @Getter
        private Integer beforeStartSeconds;
        @Getter
        private Integer beforeEndSeconds;
        @Getter
        private Integer offset = 100;
        @Getter
        private Integer schedulePeriod = 10;

        public EventCompensateProcessProperty() {
        }

        public EventCompensateProcessProperty(Integer beforeStartSeconds, Integer beforeEndSeconds) {
            this.beforeStartSeconds = beforeStartSeconds;
            this.beforeEndSeconds = beforeEndSeconds;
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

        @Override
        public String toString() {
            return "EventCompensateProcessProperty{" +
                "enabled=" + enabled +
                ", threadPoolThreadPrefix='" + threadPoolThreadPrefix + '\'' +
                ", threadPoolCoreSize=" + threadPoolCoreSize +
                ", compensateState='" + compensateState + '\'' +
                ", beforeStartSeconds=" + beforeStartSeconds +
                ", beforeEndSeconds=" + beforeEndSeconds +
                ", offset=" + offset +
                ", schedulePeriod=" + schedulePeriod +
                '}';
        }
    }

    @Override
    public String toString() {
        return "EasyEventCommonProperties{" +
            "appId='" + appId + '\'' +
            ", maxRetryCount=" + maxRetryCount +
            ", compensate=" + compensate +
            ", notify=" + notify +
            '}';
    }
}
