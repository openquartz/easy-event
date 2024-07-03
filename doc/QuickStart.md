## 使用教程

### 一、引入依赖

#### 1、引入starter依赖
 使用`disruptor`作为传输的maven pom
```xml
<dependency>
    <groupId>com.openquartz</groupId>
    <artifactId>easyevent-spring-boot-starter-disruptor</artifactId>
    <version>${lastVersion}</version>
</dependency>
```
或 使用`kafka`作为传输的maven pom 
```xml
<dependency>
    <groupId>com.openquartz</groupId>
    <artifactId>easyevent-spring-boot-starter-kafka</artifactId>
    <version>${lastVersion}</version>
</dependency>
```

或 使用`rocketmq`作为传输的maven pom (**推荐**)
```xml
<dependency>
    <groupId>com.openquartz</groupId>
    <artifactId>easyevent-spring-boot-starter-rocketmq</artifactId>
    <version>${lastVersion}</version>
</dependency>
```

#### 2、EventStorage依赖

##### 执行SQL

如果不开启分表，直接执行对应的SQL. 如果开启分表，执行表: `{table-prefix}_bus_event_entity_{sharding-index}`

```sql
CREATE TABLE ee_bus_event_entity
(
    id                 BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'eventId',
    app_id                    VARCHAR(50)  NOT NULL DEFAULT '' COMMENT 'appId',
    source_id                 BIGINT (20) NOT NULL DEFAULT 0 COMMENT 'sourceId',
    class_name                VARCHAR(50)  NOT NULL DEFAULT '' COMMENT 'Event-Class',
    error_count               TINYINT (3) NOT NULL DEFAULT 0 COMMENT '执行错误次数',
    processing_state          VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '执行状态',
    successful_subscriber     VARCHAR(512) NOT NULL DEFAULT '' COMMENT '执行成功的订阅者',
    trace_id                  VARCHAR(50)  NOT NULL DEFAULT '' COMMENT 'traceId',
    event_data                TEXT         NOT NULL COMMENT 'EventData',
    creating_owner            VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '创建者机器',
    processing_owner          VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '生产者机器',
    processing_available_date TIMESTAMP             DEFAULT NULL COMMENT '执行有效时间',
    processing_failed_reason  VARCHAR(128) NOT NULL DEFAULT '' COMMENT '已经执行失败的原因',
    created_time              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'EventEntity';
```

#### 3、引入EventTransfer依赖

需要引入`EventTransfer`的实现.目前支持`Disruptor`、`RocketMQ`、`Kafka`。
> 推荐使用`RocketMQ`作为 `EventTransfer`的分布式调度。针对`RocketMQ` 的实现做了很多的优化。例如：批量消息发送,消息拆分,消费失败自定义重试,发送失败自定义重试次数等

可以选择其中一个作为transfer实现。

### 二、配置

#### 1、common配置

|  配置key   | 描述  | 默认值 | 备注 |
|  ----  | ----  | --- | --- |
| easyevent.common.app-id  | 应用ID | |可以与spring.application.name一致 |
| easyevent.common.max-retry-count  | 最大重试次数 | 5 | |

##### 补偿配置

|  配置key   | 描述  | 默认值 | 备注 |
|  ----  | ----  | --- | --- |
| easyevent.common.compensate.thread-pool.thread-prefix  | 执行补偿线程池线程前缀 | EventCompensateThread | |
| easyevent.common.compensate.thread-pool.core-pool-size | 执行补偿线程池核心线程数 | 10 | |
| easyevent.common.compensate.thread-pool.maximum-pool-size  | 执行补偿线程池最大线程数 | 20 | |
| easyevent.common.compensate.thread-pool.keep-alive-time  | 执行补偿线程池线程空闲时间 | 30 |单位：秒 |
| easyevent.common.compensate.thread-pool.max-blocking-queue-size | 执行补偿线程池最大等待队列长度 | 2048 | |

###### 补偿当前机器配置

|  配置key   | 描述  | 默认值 | 备注 |
|  ----  | ----  | --- | --- |
| easyevent.common.compensate.self.enabled | 是否开启调度补偿 | true | |
| easyevent.common.compensate.self.offset  | 每次调度重试条数 | 100 | |
| easyevent.common.compensate.self.compensate-state|调度补偿的状态|AVAILABLE,TRANSFER_FAILED,PROCESS_FAILED||
| easyevent.common.compensate.self.before-start-seconds|调度补偿的时间范围-开始时间|10| 单位:秒|
| easyevent.common.compensate.self.before-end-seconds |调度补偿的时间范围-结束时间|60| 单位:秒 |
| easyevent.common.compensate.self.schedule-period |执行周期|10| 单位:秒 |
| easyevent.common.compensate.self.thread-pool-core-size |执行调度线程数|1||
| easyevent.common.compensate.self.thread-pool-thread-prefix|执行调度的线程名前缀|EventCompensate||

###### 全局当前机器配置

|  配置key   | 描述  | 默认值 | 备注 |
|  ----  | ----  | --- | --- |
| easyevent.common.compensate.global.enabled | 是否开启调度补偿 | true | |
| easyevent.common.compensate.global.offset  | 每次调度重试条数 | 100 | |
| easyevent.common.compensate.global.compensate-state|调度补偿的状态|AVAILABLE,TRANSFER_FAILED,PROCESS_FAILED||
| easyevent.common.compensate.global.before-start-seconds|调度补偿的时间范围-开始时间|60| 单位 : 秒|
| easyevent.common.compensate.global.before-end-seconds |调度补偿的时间范围-结束时间|3600| 单位 : 秒|
| easyevent.common.compensate.global.schedule-period |执行周期|10| 单位:秒 |
| easyevent.common.compensate.global.thread-pool-core-size |执行调度线程数|1||
| easyevent.common.compensate.global.thread-pool-thread-prefix|执行调度的线程名前缀|EventCompensate||

#### 2、bus配置

|  配置key   | 描述  | 默认值 | 备注 |
|  ----  | ----  | --- | --- |
| easyevent.bus.thread-pool.thread-prefix | eventbus处理线程池线程前缀 | DefaultEventBusThreadPool | |
| easyevent.bus.thread-pool.core-pool-size  | eventbus处理核心线程池 | 10 | |
| easyevent.bus.thread-pool.maximum-pool-size  |  eventbus处理核心线程池最大线程数 | 20 | |
| easyevent.bus.thread-pool.keep-alive-time | eventbus处理线程池最大空闲时间 | 30 | 单位:秒 |
| easyevent.bus.thread-pool.max-blocking-queue-size | eventbus处理线程池最大队列长度 | 2048 | |

#### 3、storage配置

|  配置key   | 描述  | 默认值 | 备注 |
|  ----  | ----  | --- | --- |
| easyevent.storage.jdbc.datasource.type  |  存储jdbc连接池数据源类型|  | |
| easyevent.storage.jdbc.datasource.driver-class-name | 存储jdbc连接池驱动类 | | |
| easyevent.storage.jdbc.datasource.url | 存储jdbc连接池url | | |
| easyevent.storage.jdbc.datasource.username  | 存储jdbc连接用户名 |  | |
| easyevent.storage.jdbc.datasource.password  | 存储jdbc连接密码 | | |
| easyevent.storage.jdbc.table.prefix | 存储jdbc表前缀 | ee | |
| easyevent.storage.jdbc.table.total-sharding | 存储jdbc分表数 | -1 | -1:不分表,>0 代表分表数 |

#### 4、transfer配置

|  配置key   | 描述  | 默认值 | 备注 |
|  ----  | ----  | --- | --- |
| easyevent.transfer.common.default-topic  | 默认topic | default | |
| easyevent.transfer.sender.thread-pool.thread-prefix | Transfer处理线程池线程前缀 | DefaultTransferThreadPool | |
| easyevent.transfer.sender.thread-pool.core-pool-size  | Transfer处理核心线程池 | 10 | |
| easyevent.transfer.sender.thread-pool.maximum-pool-size  | Transfer处理核心线程池最大线程数 | 20 | |
| easyevent.transfer.sender.thread-pool.keep-alive-time | Transfer处理线程池最大空闲时间 | 30 | 单位:秒 |
| easyevent.transfer.sender.thread-pool.max-blocking-queue-size | Transfers处理线程池最大队列长度 | 2048 | |

##### disruptor配置

|  配置key   | 描述  | 默认值 | 备注 |
|  ----  | ----  | --- | --- |
| easyevent.transfer.trigger.disruptor.consumer.buffer-size  |  buffer size|  | |
| easyevent.transfer.trigger.disruptor.consumer.maximum-pool-size |  | | |
| easyevent.transfer.trigger.disruptor.consumer.core-pool-size |  | | |
| easyevent.transfer.trigger.disruptor.consumer.thread-prefix |  | | |
| easyevent.transfer.trigger.disruptor.sender.thread-group  |  发送线程池组 |  easyevent-disruptor| |
| easyevent.transfer.trigger.disruptor.sender.thread-prefix| 发送线程前缀 | disruptor-thread- | |

##### rocketmq 配置

|  配置key   | 描述  | 默认值 | 备注 |
|  ----  | ----  | --- | --- |
| easyevent.transfer.trigger.rocketmq.host  | rocketmq连接地址 | 127.0.0.1:9876 |多个时使用 逗号分隔 |
| easyevent.transfer.trigger.rocketmq.produce-group | 生产者组 | EasyEvent | |
| easyevent.transfer.trigger.rocketmq.produce-latency-fault-enable | 是否开启故障转移 | true | |
| easyevent.transfer.trigger.rocketmq.produce-message-size | 发送消息大小 | 1000000 | 单位：byte |
| easyevent.transfer.trigger.rocketmq.produce-timeout  |  发送超时时间 | 1000 | 单位：秒 |
| easyevent.transfer.trigger.rocketmq.produce-try-times | 发送尝试最大次数 | 5 | |

**消费者配置**\
**格式：** `easyevent.transfer.trigger.rocketmq.consumers.<consumer alias>.x`

|  配置key   | 描述  | 默认值 | 备注 |
|  ----  | ----  | --- | --- |
| easyevent.transfer.trigger.rocketmq.consumers.<consumer alias>.consumer-group  | 消费者组 |  | |
| easyevent.transfer.trigger.rocketmq.consumers.<consumer alias>.topic | 消费topic | EasyEvent | |
| easyevent.transfer.trigger.rocketmq.consumers.<consumer alias>.tags | 消费tag | * | 多个tag以逗号分隔 |
| easyevent.transfer.trigger.rocketmq.consumers.<consumer alias>.consumer-min-thread |  消费者最小线程数 | 1 | |
| easyevent.transfer.trigger.rocketmq.consumers.<consumer alias>.consumer-max-thread | 消费者最大线程数 | 3 | |
| easyevent.transfer.trigger.rocketmq.consumers.<consumer alias>.consume-concurrently-max-span | 消费者最大并发span | 10 | |
| easyevent.transfer.trigger.rocketmq.consumers.<consumer alias>.consume-max-retry | 消费最大重试次数 | 5 | |
| easyevent.transfer.trigger.rocketmq.consumers.<consumer alias>.consume-retry-delay-time-interval-seconds | 消费重试间隔 | 5 | 单位：秒 |
| easyevent.transfer.trigger.rocketmq.consumers.<consumer alias>.consume-liming-retry-delay-time-base-seconds | 消费限流基础重试间隔 | 5 | 单位：秒 |

##### kafka 配置

|  配置key   | 描述  | 默认值 | 备注 |
|  ----  | ----  | --- | --- |
| easyevent.transfer.trigger.kafka.host  | kafka集群地址 | 127.0.0.1:9876 | 多个地址以逗号分隔 |
| easyevent.transfer.trigger.kafka.produce-group | 生产者组 | EasyEvent | |
| easyevent.transfer.trigger.kafka.produce-topic-partitions | topic对应的分区数 | 4 | |
| easyevent.transfer.trigger.kafka.produce-timeout  |  发送超时时间 | 1000 | 单位：秒 |
| easyevent.transfer.trigger.kafka.produce-try-times | 发送尝试最大次数 | 5 | |

**消费者配置**\
**格式：** `easyevent.transfer.trigger.kafka.consumers.<consumer alias>.x`\
同一个topic下如果已经指定分区了,需要将全部分区都配置且不能配置有 `*` 的分区。

|  配置key   | 描述  | 默认值 | 备注 |
|  ----  | ----  | --- | --- |
| easyevent.transfer.trigger.kafka.consumers.<consumer alias>.consumer-group  | 消费者组 |  | |
| easyevent.transfer.trigger.kafka.consumers.<consumer alias>.topic | 消费topic | EasyEvent | |
| easyevent.transfer.trigger.kafka.consumers.<consumer alias>.partition | 分区 | * | 默认是所有分区 |
| easyevent.transfer.trigger.kafka.consumers.<consumer alias>.currency |  消费并发数 | 4 | |
| easyevent.transfer.trigger.kafka.consumers.<consumer alias>.client-id | 消费者ClientId |  | |
| easyevent.transfer.trigger.kafka.consumers.<consumer alias>.consume-max-retry | 消费者最大重试次数 | 5 | |
| easyevent.transfer.trigger.kafka.consumers.<consumer alias>.consume-retry-delay-time-interval-seconds | 消费重试时间间隔 | 5 | 单位：秒|

### 三、编写代码

#### 发布事件

`EasyEvent` 发布事件的统一入口为`com.openquartz.easyevent.core.publisher.EventPublisher`。可以使用Spring的直接注入到方法中使用进行发布事件类。 目前支持发布**同步事件**
和**异步事件**。
**同步事件**：指和当前发布事件线程在同一线程中触发执行;
**异步事件**：指的是通过中间件进行异步调度然后最终一致性实现订阅处理的事件。

样例：

```java
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;
import com.openquartz.easyevent.core.publisher.EventPublisher;
import com.openquartz.easyevent.example.event.OrderCompletedEvent;

/**
 * @author svnee
 **/
@Component
public class TestEventPublisher {

    @Resource
    private EventPublisher eventPublisher;

    public void publish(TestEvent testEvent) {
        eventPublisher.syncPublish(testEvent);
    }

    public void asyncPublish(TestEvent event) {
        eventPublisher.asyncPublish(event);
    }

    public void asyncPublishList(List<TestEvent> eventList) {
        ArrayList<Object> list = new ArrayList<>(eventList);
        eventPublisher.asyncPublishList(list);
    }

}

```

#### 订阅事件

订阅者需要在类上加上注解(`com.openquartz.easyevent.starter.annotation.EventHandler`)来标识这是一个Event处理类。

同时在订阅的方法上加上注解(`com.openquartz.easyevent.core.annotation.Subscribe`)可以标识这个方法在订阅参数中的事件。
> 支持按条件订阅。可以在注解@Subscriube上添加属性condition。支持`SPEL` 表达式.

`EasyEvent` 订阅者订阅事件,目前支持**串行订阅事件**和**并行订阅事件**。
**串行订阅事件**：默认是串行订阅。和当前主线程在同一个线程中,一起成功,或者一起失败。
允许进行编排顺序。可以通过加上注解(`com.openquartz.easyevent.core.annotation.Order`)在订阅方法上，其中值越小越优先执行。
> 针对串行订阅事件。可以选择是否加入到事件完成的事务中。可以配置属性`joinTransaction=false`.默认加入一起事务中。\
> 使用场景: 针对一些RPC的场景，可以使用`joinTransaction=false`.以减少事务执行时长.避免大事务发生。

**并行订阅事件**：并行订阅事件。指订阅者之间是互不影响，独立触发执行。使用并行订阅线程池触发执行。
可以通过加上注解(`com.openquartz.easyevent.core.annotation.AllowConcurrentEvents`)在订阅方法上。

```java
import com.openquartz.easyevent.core.annotation.AllowConcurrentEvents;
import com.openquartz.easyevent.core.annotation.Subscribe;
import com.openquartz.easyevent.example.event.OrderCompletedEvent;
import com.openquartz.easyevent.starter.annotation.EventHandler;

/**
 * @author svnee
 **/
@EventHandler
public class Test2EventHandler {

    @Subscribe
    @AllowConcurrentEvents
    public void handle(TestEvent event) {
        // do 业务处理
    }
}
```

### 四、预警通知(可选)

服务提供重试达到最大次数的事件仍然未能成功的进行预警。实现接口`com.openquartz.easyevent.core.notify.EventHandleFailedNotifier`。\
默认实现为`com.openquartz.easyevent.starter.schedule.DefaultEventHandleFailedNotifier`.

预警通知接口为: `com.openquartz.easyevent.core.notify.EventNotifier`

```java
package com.openquartz.easyevent.core.notify;

import java.util.List;
import com.openquartz.easyevent.storage.model.BusEventEntity;

/**
 * event notifier
 *
 * @author svnee
 */
public interface EventNotifier {

    /**
     * notify event
     *
     * @param eventList eventList
     */
    void notify(List<BusEventEntity> eventList);
}
```

默认实现是`com.openquartz.easyevent.core.notify.LogEventNotifier`。 用户可以自定义实现进行使用不同方式通知,并注入到Spring工厂中。例如:`微信`,`钉钉`等。

同时默认提供了预警自定义配置

|  配置key   | 描述  | 默认值 | 备注 |
|  ----  | ----  | --- | --- |
|  easyevent.common.notify.enabled | 是否启用预警 | true | |
|  easyevent.common.notify.identify | 通知唯一标识 | EventFailedNotifier | |
|  easyevent.common.notify.period  | 通知周期 | 10 | 单位：分钟 |
|  easyevent.common.notify.thread-prefix  | 通知线程前缀名称 | EventNotifierThread | |

如果需要支持分布式预警通知,需要用于提供实现接口`com.openquartz.easyevent.common.concurrent.lock.DistributedLockFactory`并注入到Spring工厂中。