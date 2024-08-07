package com.openquartz.easyevent.starter.spring.boot.autoconfig.property;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DefaultTransferProperties
 *
 * @author svnee
 **/
@Data
@Slf4j
@ConfigurationProperties(prefix = DefaultTransferProperties.PREFIX)
public class DefaultTransferProperties {

    public static final String PREFIX = "easyevent.transfer.common";

    /**
     * 默认路由-topic
     */
    private String defaultTopic = "easyevent";

    @Override
    public String toString() {
        return "DefaultTransferProperties{" +
            "defaultTopic='" + defaultTopic + '\'' +
            '}';
    }
}
