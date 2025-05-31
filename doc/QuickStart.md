## Tutorial

### I. Install Locally

Execute the following commands:
Enter the root directory of the `easy-event` project.

#### 1. First install easyevent-common

```shell
cd easyevent-common
mvn clean install
```

#### 2. Next install easyevent-storage

```shell
cd ./../easyevent-storage
mvn clean install
```

#### 3. Install easyevent-transfer

```shell
cd ./../easyevent-transfer
mvn clean install
```

#### 4. Install easyevent-core

```shell
cd ./../easyevent-core
mvn clean install
```

#### 5. Install easyevent-spring-boot-starter

```shell
cd ./../easyevent-spring-boot-starter
mvn clean install
```

#### 6. Install easyevent-spring-boot-starter-soa

```shell
cd ./../easyevent-spring-boot-starter-soa
mvn clean install
```

### II. Add Dependencies

#### 1. Add starter dependencies

Using [disruptor](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/easyevent-transfer/easyevent-transfer-disruptor/src/main/java/com/openquartz/easyevent/transfer/disruptor/DisruptorTriggerEventSender.java#L45-L45) as the transport:

```xml
<dependency>
    <groupId>com.openquartz</groupId>
    <artifactId>easyevent-spring-boot-starter-disruptor</artifactId>
    <version>${lastVersion}</version>
</dependency>
```

Or using `kafka` as the transport:

```xml
<dependency>
    <groupId>com.openquartz</groupId>
    <artifactId>easyevent-spring-boot-starter-kafka</artifactId>
    <version>${lastVersion}</version>
</dependency>
```

Or using `rocketmq` as the transport (**Recommended**):

```xml
<dependency>
    <groupId>com.openquartz</groupId>
    <artifactId>easyevent-spring-boot-starter-rocketmq</artifactId>
    <version>${lastVersion}</version>
</dependency>
```

#### 2. EventStorage Dependency

##### Execute SQL

If table sharding is not enabled, execute the corresponding SQL directly. If it is enabled, execute on the table: `{table-prefix}_bus_event_entity_{sharding-index}`

```sql
CREATE TABLE ee_bus_event_entity
(
    id                        BIGINT (20) NOT NULL AUTO_INCREMENT COMMENT 'eventId',
    app_id                    VARCHAR(50)  NOT NULL DEFAULT '' COMMENT 'appId',
    source_id                 BIGINT (20) NOT NULL DEFAULT 0 COMMENT 'sourceId',
    class_name                VARCHAR(128) NOT NULL DEFAULT '' COMMENT 'Event-Class',
    error_count               TINYINT (3) NOT NULL DEFAULT 0 COMMENT 'execution error count',
    processing_state          VARCHAR(50)  NOT NULL DEFAULT '' COMMENT 'execution status',
    successful_subscriber     VARCHAR(512) NOT NULL DEFAULT '' COMMENT 'subscribers that succeeded',
    trace_id                  VARCHAR(50)  NOT NULL DEFAULT '' COMMENT 'traceId',
    event_data                TEXT         NOT NULL COMMENT 'EventData',
    event_key                 VARCHAR(128) NOT NULL DEFAULT '' COMMENT 'EventKey',
    creating_owner            VARCHAR(50)  NOT NULL DEFAULT '' COMMENT 'creator machine',
    processing_owner          VARCHAR(50)  NOT NULL DEFAULT '' COMMENT 'producer machine',
    processing_available_date TIMESTAMP             DEFAULT NULL COMMENT 'execution available time',
    processing_failed_reason  VARCHAR(128) NOT NULL DEFAULT '' COMMENT 'reason for execution failure',
    created_time              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'creation time',
    updated_time              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    PRIMARY KEY (id),
    INDEX                     idx_event_key (event_key),
    INDEX                     idx_app_state_owner_time(app_id, processing_state, processing_owner, created_time)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT 'ee_bus_event_entity';
```

#### 3. Introduce EventTransfer Dependency

You need to include an implementation of `EventTransfer`. Currently supported are `Disruptor`, `RocketMQ`, and `Kafka`.
> It's recommended to use `RocketMQ` as the distributed scheduler for `EventTransfer`. Many optimizations have been made in its implementation, such as batch message sending, message splitting, custom retry after consumer failure, and custom retry attempts when sending fails.

You can choose one of them as the transfer implementation.

### III. Configuration

#### 1. Common Configuration

| Configuration Key                            | Description        | Default Value | Remarks                           |
|--------------------------------------------|--------------------|---------------|-----------------------------------|
| easyevent.common.app-id                    | Application ID     |               | Can be consistent with spring.application.name |
| easyevent.common.max-retry-count           | Max retry count    | 5             |                                   |

##### Compensation Configuration

| Configuration Key                                                          | Description                         | Default Value           | Remarks      |
|---------------------------------------------------------------------------|-------------------------------------|--------------------------|--------------|
| easyevent.common.compensate.thread-pool.thread-prefix                     | Thread prefix for compensation thread pool | EventCompensateThread |              |
| easyevent.common.compensate.thread-pool.core-pool-size                    | Core thread count for compensation thread pool | 10                      |              |
| easyevent.common.compensate.thread-pool.maximum-pool-size                 | Max thread count for compensation thread pool | 20                      |              |
| easyevent.common.compensate.thread-pool.keep-alive-time                  | Idle timeout for compensation threads (seconds) | 30                      |              |
| easyevent.common.compensate.thread-pool.max-blocking-queue-size           | Max waiting queue size for compensation thread pool | 2048                  |              |

###### Local Machine Compensation Configuration

| Configuration Key                                                         | Description                              | Default Value                                     | Remarks   |
|---------------------------------------------------------------------------|------------------------------------------|--------------------------------------------------|-----------|
| easyevent.common.compensate.self.enabled                                  | Enable local scheduling compensation       | true                                             |           |
| easyevent.common.compensate.self.offset                                   | Number of retries per scheduling           | 100                                              |           |
| easyevent.common.compensate.self.compensate-state                         | Statuses for scheduling compensation       | AVAILABLE,TRANSFER_FAILED,PROCESS_FAILED         |           |
| easyevent.common.compensate.self.before-start-seconds                     | Start time range for compensation (seconds) | 10                                               |           |
| easyevent.common.compensate.self.before-end-seconds                       | End time range for compensation (seconds)   | 60                                               |           |
| easyevent.common.compensate.self.schedule-period                          | Execution period (seconds)                 | 10                                               |           |
| easyevent.common.compensate.self.thread-pool-core-size                    | Scheduling thread count                    | 1                                                |           |
| easyevent.common.compensate.self.thread-pool-thread-prefix                | Prefix for scheduling threads              | EventCompensate                                |           |

###### Global Machine Compensation Configuration

| Configuration Key                                                        | Description                               | Default Value                                      | Remarks     |
|--------------------------------------------------------------------------|-------------------------------------------|--------------------------------------------------|-------------|
| easyevent.common.compensate.global.enabled                               | Enable global scheduling compensation       | true                                             |             |
| easyevent.common.compensate.global.offset                                | Number of retries per scheduling            | 100                                              |             |
| easyevent.common.compensate.global.compensate-state                      | Statuses for scheduling compensation        | AVAILABLE,TRANSFER_FAILED,PROCESS_FAILED        |             |
| easyevent.common.compensate.global.before-start-seconds                  | Start time range for compensation (seconds) | 60                                               | Unit: seconds |
| easyevent.common.compensate.global.before-end-seconds                    | End time range for compensation (seconds)   | 3600                                             | Unit: seconds |
| easyevent.common.compensate.global.schedule-period                        | Execution period (seconds)                  | 10                                               |             |
| easyevent.common.compensate.global.thread-pool-core-size                 | Scheduling thread count                     | 1                                                |             |
| easyevent.common.compensate.global.thread-pool-thread-prefix             | Prefix for scheduling threads               | EventCompensate                                |             |

#### 2. Bus Configuration

| Configuration Key                                          | Description                             | Default Value           | Remarks   |
|------------------------------------------------------------|-----------------------------------------|--------------------------|-----------|
| easyevent.bus.thread-pool.thread-prefix                    | Thread prefix for EventBus thread pool | DefaultEventBusThreadPool |           |
| easyevent.bus.thread-pool.core-pool-size                   | Core thread count for EventBus         | 10                        |           |
| easyevent.bus.thread-pool.maximum-pool-size                | Max thread count for EventBus          | 20                        |           |
| easyevent.bus.thread-pool.keep-alive-time                  | Idle timeout for EventBus threads (seconds) | 30                     |           |
| easyevent.bus.thread-pool.max-blocking-queue-size          | Max queue length for EventBus thread pool | 2048                    |           |

#### 3. Storage Configuration

| Configuration Key                                         | Description                   | Default Value | Remarks                       |
|-----------------------------------------------------------|-------------------------------|---------------|-------------------------------|
| easyevent.storage.jdbc.datasource.type                    | JDBC connection pool type     |               |                               |
| easyevent.storage.jdbc.datasource.driver-class-name       | JDBC driver class             |               |                               |
| easyevent.storage.jdbc.datasource.url                     | JDBC URL                      |               |                               |
| easyevent.storage.jdbc.datasource.username                | JDBC username                 |               |                               |
| easyevent.storage.jdbc.datasource.password                | JDBC password                 |               |                               |
| easyevent.storage.jdbc.table.prefix                       | Table prefix for JDBC storage | ee            |                               |
| easyevent.storage.jdbc.table.total-sharding               | Total number of shards        | -1            | -1: No sharding; >0: Shard count |

#### 4. Transfer Configuration

| Configuration Key                                                       | Description                           | Default Value           | Remarks   |
|--------------------------------------------------------------------------|---------------------------------------|--------------------------|-----------|
| easyevent.transfer.common.default-topic                                 | Default topic                         | default                  |           |
| easyevent.transfer.sender.thread-pool.thread-prefix                     | Thread prefix for Transfer thread pool | DefaultTransferThreadPool |           |
| easyevent.transfer.sender.thread-pool.core-pool-size                    | Core thread count for Transfer         | 10                       |           |
| easyevent.transfer.sender.thread-pool.maximum-pool-size                 | Max thread count for Transfer          | 20                       |           |
| easyevent.transfer.sender.thread-pool.keep-alive-time                   | Idle timeout for Transfer threads (seconds) | 30                    |           |
| easyevent.transfer.sender.thread-pool.max-blocking-queue-size           | Max queue length for Transfer thread pool | 2048                   |           |

##### Disruptor Configuration

| Configuration Key                                                   | Description           | Default Value | Remarks |
|----------------------------------------------------------------------|-----------------------|---------------|---------|
| easyevent.transfer.trigger.disruptor.consumer.buffer-size           | Buffer size           |               |         |
| easyevent.transfer.trigger.disruptor.consumer.maximum-pool-size     | Max thread count      |               |         |
| easyevent.transfer.trigger.disruptor.consumer.core-pool-size        | Core thread count     |               |         |
| easyevent.transfer.trigger.disruptor.consumer.thread-prefix         | Thread prefix         |               |         |
| easyevent.transfer.trigger.disruptor.sender.thread-group            | Sender thread group   | easyevent-disruptor |         |
| easyevent.transfer.trigger.disruptor.sender.thread-prefix           | Sender thread prefix  | disruptor-thread- |         |

##### RocketMQ Configuration

| Configuration Key                                                    | Description                  | Default Value         | Remarks             |
|----------------------------------------------------------------------|------------------------------|------------------------|----------------------|
| easyevent.transfer.trigger.rocketmq.host                            | RocketMQ connection address | 127.0.0.1:9876       | Multiple addresses separated by commas |
| easyevent.transfer.trigger.rocketmq.produce-group                   | Producer group              | EasyEvent             |                      |
| easyevent.transfer.trigger.rocketmq.produce-latency-fault-enable    | Enable latency fault tolerance | true                 |                      |
| easyevent.transfer.trigger.rocketmq.produce-message-size            | Message size (bytes)        | 1000000               |                      |
| easyevent.transfer.trigger.rocketmq.produce-timeout                 | Timeout for sending (seconds) | 1000                 |                      |
| easyevent.transfer.trigger.rocketmq.produce-try-times               | Max retry attempts          | 5                       |                      |

**Consumer Configuration**  
**Format:** `easyevent.transfer.trigger.rocketmq.consumers.<consumer alias>.x`

| Configuration Key                                                                                             | Description           | Default Value       | Remarks               |
|---------------------------------------------------------------------------------------------------------------|-----------------------|----------------------|------------------------|
| easyevent.transfer.trigger.rocketmq.consumers.<consumer alias>.consumer-group                               | Consumer group        |                      |                        |
| easyevent.transfer.trigger.rocketmq.consumers.<consumer alias>.topic                                        | Topic                 | EasyEvent            |                        |
| easyevent.transfer.trigger.rocketmq.consumers.<consumer alias>.tags                                         | Tags                  | *                    | Multiple tags separated by commas |
| easyevent.transfer.trigger.rocketmq.consumers.<consumer alias>.consumer-min-thread                          | Minimum consumer threads | 1                   |                        |
| easyevent.transfer.trigger.rocketmq.consumers.<consumer alias>.consumer-max-thread                          | Maximum consumer threads | 3                   |                        |
| easyevent.transfer.trigger.rocketmq.consumers.<consumer alias>.consume-concurrently-max-span                | Max concurrent span   | 10                   |                        |
| easyevent.transfer.trigger.rocketmq.consumers.<consumer alias>.consume-max-retry                            | Max retry attempts    | 5                    |                        |
| easyevent.transfer.trigger.rocketmq.consumers.<consumer alias>.consume-retry-delay-time-interval-seconds    | Retry interval (seconds) | 5                  |                        |
| easyevent.transfer.trigger.rocketmq.consumers.<consumer alias>.consume-liming-retry-delay-time-base-seconds | Base retry delay for rate limiting (seconds) | 5 |                        |

##### Kafka Configuration

| Configuration Key                                            | Description           | Default Value         | Remarks               |
|--------------------------------------------------------------|-----------------------|------------------------|------------------------|
| easyevent.transfer.trigger.kafka.host                        | Kafka cluster address | 127.0.0.1:9876       | Multiple addresses separated by commas |
| easyevent.transfer.trigger.kafka.produce-group               | Producer group        | EasyEvent             |                        |
| easyevent.transfer.trigger.kafka.produce-topic-partitions    | Partitions for topic  | 4                       |                        |
| easyevent.transfer.trigger.kafka.produce-timeout             | Timeout for sending (seconds) | 1000         |                        |
| easyevent.transfer.trigger.kafka.produce-try-times           | Max retry attempts    | 5                       |                        |

**Consumer Configuration**  
**Format:** `easyevent.transfer.trigger.kafka.consumers.<consumer alias>.x`  
If partitions are specified for a topic, all partitions must be configured and cannot contain [*](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/doc/QuickStart.md).

| Configuration Key                                                                                     | Description           | Default Value       | Remarks       |
|-------------------------------------------------------------------------------------------------------|-----------------------|----------------------|----------------|
| easyevent.transfer.trigger.kafka.consumers.<consumer alias>.consumer-group                          | Consumer group        |                      |                |
| easyevent.transfer.trigger.kafka.consumers.<consumer alias>.topic                                   | Topic                 | EasyEvent            |                |
| easyevent.transfer.trigger.kafka.consumers.<consumer alias>.partition                               | Partition             | *                    | Default: all   |
| easyevent.transfer.trigger.kafka.consumers.<consumer alias>.currency                                | Consumption concurrency | 4                   |                |
| easyevent.transfer.trigger.kafka.consumers.<consumer alias>.client-id                               | ClientId              |                      |                |
| easyevent.transfer.trigger.kafka.consumers.<consumer alias>.consume-max-retry                       | Max retry attempts    | 5                    |                |
| easyevent.transfer.trigger.kafka.consumers.<consumer alias>.consume-retry-delay-time-interval-seconds | Retry interval (seconds) | 5                  |                |

### IV. Writing Code

#### Publishing Events

The unified entry point for publishing events in `EasyEvent` is [com.openquartz.easyevent.core.publisher.EventPublisher](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/easyevent-core/src/main/java/com/openquartz/easyevent/core/publisher/EventPublisher.java#L7-L35). You can inject this into methods using Spring for event publishing.
Currently, **synchronous events** and **asynchronous events** are supported.
- **Synchronous Event**: Triggered in the same thread as the current publishing thread.
- **Asynchronous Event**: Triggered asynchronously via middleware, achieving eventual consistency in subscription handling.

Example:

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

#### Subscribing to Events

Subscribers should annotate their classes with ([com.openquartz.easyevent.starter.annotation.EventHandler](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/easyevent-spring-boot-starter/easyevent-spring-boot-starter-parent/src/main/java/com/openquartz/easyevent/starter/annotation/EventHandler.java#L15-L30)) to identify them as event handlers.

Additionally, annotate the method with ([com.openquartz.easyevent.core.annotation.Subscribe](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/easyevent-core/src/main/java/com/openquartz/easyevent/core/annotation/Subscribe.java#L23-L36)) to indicate that the method subscribes to the event passed as a parameter.
> Conditional subscriptions are supported. You can add the [condition](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/easyevent-core/src/main/java/com/openquartz/easyevent/core/annotation/Subscribe.java#L30-L30) attribute to the [@Subscribe](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/easyevent-core/src/main/java/com/openquartz/easyevent/core/annotation/Subscribe.java#L23-L36) annotation. `SPEL` expressions are supported.

`EasyEvent` subscribers currently support **serial subscription** and **parallel subscription**.
- **Serial Subscription**: Serial subscription is the default. It runs in the same thread as the main thread, succeeding or failing together.
Ordering can be customized by adding the annotation ([com.openquartz.easyevent.core.annotation.Order](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/easyevent-core/src/main/java/com/openquartz/easyevent/core/annotation/Order.java#L13-L23)) to the subscribed method, where smaller values are executed first.
> For serial subscriptions, you can choose whether to join the transaction upon event completion. Set the property `joinTransaction=false`. By default, it joins the transaction.
> Use case: For RPC scenarios, `joinTransaction=false` can be used to reduce transaction execution time and avoid large transactions.

- **Parallel Subscription**: Parallel subscription means subscribers operate independently and are triggered separately. Execution is triggered using a parallel subscription thread pool.
This can be achieved by annotating the subscription method with ([com.openquartz.easyevent.core.annotation.AllowConcurrentEvents](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/easyevent-core/src/main/java/com/openquartz/easyevent/core/annotation/AllowConcurrentEvents.java#L16-L18)).

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
        // Business logic here
    }
}
```

### V. Alert Notifications (Optional)

An alert will be triggered if an event fails even after reaching the maximum number of retries. Implement the interface [com.openquartz.easyevent.core.notify.EventHandleFailedNotifier](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/easyevent-core/src/main/java/com/openquartz/easyevent/core/notify/EventHandleFailedNotifier.java#L7-L14).
The default implementation is [com.openquartz.easyevent.starter.schedule.DefaultEventHandleFailedNotifier](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/easyevent-spring-boot-starter/easyevent-spring-boot-starter-parent/src/main/java/com/openquartz/easyevent/starter/schedule/DefaultEventHandleFailedNotifier.java#L26-L65).

The alert notification interface is: [com.openquartz.easyevent.core.notify.EventNotifier](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/easyevent-core/src/main/java/com/openquartz/easyevent/core/notify/EventNotifier.java#L10-L18)

```java
package com.openquartz.easyevent.core.notify;

import java.util.List;

import com.openquartz.easyevent.storage.model.BusEventEntity;

/**
 * Event notifier
 *
 * @author svnee
 */
public interface EventNotifier {

    /**
     * Notify about events
     *
     * @param eventList List of events
     */
    void notify(List<BusEventEntity> eventList);
}
```

The default implementation is [com.openquartz.easyevent.core.notify.LogEventNotifier](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/easyevent-core/src/main/java/com/openquartz/easyevent/core/notify/LogEventNotifier.java#L15-L31). Users can customize implementations for different notification methods and inject them into the Spring factory. Examples include WeChat, DingTalk, etc.

Additionally, default configurations for alerts are provided:

| Configuration Key                                | Description          | Default Value         | Remarks     |
|--------------------------------------------------|----------------------|------------------------|--------------|
| easyevent.common.notify.enabled                | Enable alerts        | true                   |              |
| easyevent.common.notify.identify               | Unique identifier for notifications | EventFailedNotifier |              |
| easyevent.common.notify.period                 | Notification interval (minutes) | 10          |              |
| easyevent.common.notify.thread-prefix          | Thread prefix for notifications | EventNotifierThread |              |

If distributed alert notifications are required, users need to provide an implementation of the interface [com.openquartz.easyevent.common.concurrent.lock.DistributedLockFactory](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/easyevent-common/src/main/java/com/openquartz/easyevent/common/concurrent/lock/DistributedLockFactory.java#L10-L20) and inject it into the Spring factory.

### VI. SOA Event Support

Easy-Event provides support for event centers in SOA microservices.

#### 1. Add Dependencies

Introduce these dependencies based on normal single-service events. Currently, RocketMQ-based event centers for SOA are provided. Developers can also extend other middleware-based event centers.

```xml
<dependency>
    <groupId>com.openquartz</groupId>
    <artifactId>easyevent-spring-boot-starter-soa-rocketmq</artifactId>
    <version>${revesion}</version>
</dependency>
```

#### 2. Define SOA Events

It is recommended to define SOA service event classes in a common module, publish them to a repository, and have various services depend on them.
SOA events require the following dependency:

```xml
<dependency>
    <groupId>com.openquartz</groupId>
    <artifactId>easyevent-spring-boot-starter-soa-api</artifactId>
    <version>${revesion}</version>
</dependency>
```

SOA events need to implement the [com.openquartz.easyevent.starter.soa.api.SoaEvent](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/easyevent-spring-boot-starter-soa/easyevent-spring-boot-starter-soa-api/src/main/java/com/openquartz/easyevent/starter/soa/api/SoaEvent.java#L7-L23) interface.
Override the methods:

```java
@Override
public String getSoaIdentify() {
    // Application event appId
}

// Optional
@Override
public String getEventKey() {
    // Easily searchable event key
}
```

#### 3. Configuration

Configuration needed:
```properties
easyevent.soa.event-center.rocketmq.host=localhost:9876
easyevent.soa.event-center.rocketmq.topic=event_center
easyevent.soa.event-center.rocketmq.produce-group=easyevent-soa-publisher-produce-group
easyevent.soa.event-center.rocketmq.consume-group=easyevent-soa-publisher-consume-group
```

#### 4. Publish & Subscribe

SOA events only support asynchronous publishing and do not support synchronous publishing.
You can use the interfaces for publishing: `com.openquartz.easyevent.core.publisher.EventPublisher.asyncPublish`, `com.openquartz.easyevent.core.publisher.EventPublisher.asyncPublishList`

Subscription remains consistent with previous consumption mechanisms.