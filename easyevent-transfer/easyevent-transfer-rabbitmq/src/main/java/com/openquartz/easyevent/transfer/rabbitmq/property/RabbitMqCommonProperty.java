package com.openquartz.easyevent.transfer.rabbitmq.property;

import lombok.Data;

/**
 * RabbitMQ Common Property
 *
 * @author svnee
 **/
@Data
public class RabbitMqCommonProperty {

    /**
     * RabbitMQ host
     */
    private String host = "localhost";

    /**
     * RabbitMQ port
     */
    private int port = 5672;

    /**
     * RabbitMQ username
     */
    private String username = "guest";

    /**
     * RabbitMQ password
     */
    private String password = "guest";

    /**
     * RabbitMQ virtual host
     */
    private String virtualHost = "/";

    /**
     * 是否异步发送
     */
    private boolean produceAsync = false;

    /**
     * 发送超时时间(毫秒)
     */
    private long produceTimeout = 3000L;

    /**
     * 发送重试次数
     */
    private int produceTryTimes = 3;

    /**
     * 生产者主题分区数
     */
    private int produceTopicPartitions = 4;

    /**
     * 消费者线程数
     */
    private int consumerThreads = 1;

    /**
     * 消费者最大线程数
     */
    private int consumerMaxThreads = 10;

    /**
     * 消费者队列大小
     */
    private int consumerQueueSize = 1000;

}