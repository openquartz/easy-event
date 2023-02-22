package org.svnee.easyevent.transfer.rocket.property;

import java.util.Map;
import lombok.Data;

/**
 * RocketMQ Trigger 配置
 *
 * @author svnee
 **/
@Data
public class RocketMqTriggerProperty {

    /**
     * rocketmq-consumer-property
     */
    private Map<String, RocketMqConsumerProperty> consumerPropertyMap;
}
