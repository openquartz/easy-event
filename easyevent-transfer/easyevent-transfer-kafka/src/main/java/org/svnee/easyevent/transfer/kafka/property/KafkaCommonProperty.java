package org.svnee.easyevent.transfer.kafka.property;

import lombok.Data;

/**
 * @author svnee
 **/
@Data
public class KafkaCommonProperty {

    /**
     * host
     */
    private String host = "127.0.0.1:9876";

    /**
     * produce topic partitions
     * 默认topic分区数
     */
    private Integer produceTopicPartitions = 4;

    /**
     * 发送超时
     * 单位：秒
     */
    private Integer produceTimeout = 1000;

    /**
     * 重试次数
     */
    private int produceTryTimes = 5;
}
