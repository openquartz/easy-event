## Extension

### Interception

EasyEvent provides interception at three stages of event processing: **before/after publishing**, **before/after triggering**, and **before/after handling**. Users can implement custom logic for unified interception according to their needs.

#### Before/After Publishing Interception

Interception is performed before and after the completion of event publishing when invoking [com.openquartz.easyevent.core.publisher.EventPublisher](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/easyevent-core/src/main/java/com/openquartz/easyevent/core/publisher/EventPublisher.java#L7-L35).  
The interception interface is: [com.openquartz.easyevent.core.intreceptor.PublisherInterceptor](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/easyevent-core/src/main/java/com/openquartz/easyevent/core/intreceptor/PublisherInterceptor.java#L9-L42), which should be injected into the Spring factory.

```java
package com.openquartz.easyevent.core.intreceptor;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Synchronous interceptor
 *
 * @author svnee
 */
public interface PublisherInterceptor {

    /**
     * Default interception order
     *
     * @return Order
     */
    default int order() {
        return Integer.MAX_VALUE;
    }

    /**
     * Before publishing starts
     *
     * @param event Event object
     * @param context Context
     * @return true - proceed to next interceptor; false - response already completed, return directly
     */
    default boolean prePublish(Object event, PublisherInterceptorContext context) {
        return true;
    }

    /**
     * After publishing completes
     *
     * @param event Event object
     * @param context Context
     * @param ex Exception if any occurred
     */
    default void afterCompletion(Object event, PublisherInterceptorContext context, @Nullable Exception ex) {

    }
}
```

#### Before/After Triggering Interception

Interception is performed before and after asynchronous event triggering via `EventTrigger`. The interception interface provided is `com.openquartz.easyevent.core.intreceptor.TriggerInterceptor`.

```java
package com.openquartz.easyevent.core.intreceptor;

import org.checkerframework.checker.nullness.qual.Nullable;
import com.openquartz.easyevent.transfer.api.message.EventMessage;

/**
 * Trigger Interceptor
 *
 * @author svnee
 */
public interface TriggerInterceptor {

    /**
     * Default interception order
     *
     * @return Order
     */
    default int order() {
        return Integer.MAX_VALUE;
    }

    /**
     * Before processing starts
     *
     * @param message Trigger message
     * @param context Context
     * @return Trigger flag
     */
    default boolean preTrigger(EventMessage message, TriggerInterceptorContext context) {
        return true;
    }

    /**
     * After processing completes
     *
     * @param message Message
     * @param context Context
     * @param ex Exception if any occurred
     */
    default void afterCompletion(EventMessage message, TriggerInterceptorContext context, @Nullable Exception ex) {
    }
}
```

#### Before/After Handling Interception

Interception occurs before and after invoking subscribers to execute business logic upon event triggering. The interception interface provided is `com.openquartz.easyevent.core.intreceptor.HandlerInterceptor`.

```java
package com.openquartz.easyevent.core.intreceptor;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Handle Interceptor
 *
 * @author svnee
 */
public interface HandlerInterceptor<T> {

    /**
     * Default interception order
     *
     * @return Order
     */
    default int order() {
        return Integer.MAX_VALUE;
    }

    /**
     * Before handling starts
     *
     * @param event Event
     * @param handler Handler
     * @param context Context
     * @return true - proceed to next interceptor; false - response already completed, return directly
     */
    default boolean preHandle(T event, Object handler, HandlerInterceptorContext context) {
        return true;
    }

    /**
     * After handling completes
     *
     * @param event Event
     * @param handler Handler
     * @param context Context
     * @param ex Exception if any occurred
     */
    default void afterCompletion(T event, Object handler, HandlerInterceptorContext context,
        @Nullable Exception ex) {
    }
}
```

### Routing

EasyEvent supports custom routing of asynchronous events to different queue topics. By default, events are published to the configured topic: `easyevent.transfer.common.default-topic`.  
If users need to send different messages to different topics, they can implement the interface [com.openquartz.easyevent.transfer.api.route.EventRouter](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/easyevent-transfer/easyevent-transfer-api/src/main/java/com/openquartz/easyevent/transfer/api/route/EventRouter.java#L9-L19).

```java
package com.openquartz.easyevent.transfer.api.route;

import com.openquartz.easyevent.common.model.Pair;

/**
 * Event routing service
 *
 * @author svnee
 */
public interface EventRouter {

    /**
     * Route event to a topic
     *
     * @param event Event object
     * @return Routing topic. Key: topic, Value: implementation-specific (e.g., RocketMQ tag, Kafka partition). Can be null.
     */
    Pair<String, String> route(Object event);
}
```

If a custom router is implemented, consumer configurations need to be added in the configuration:
`easyevent.transfer.trigger.<mq-alias>.consumers.<consumer-alias>.<property>`

Example:

```properties
easyevent.transfer.trigger.rocketmq.consumers.test.consumer-group=test1
easyevent.transfer.trigger.rocketmq.consumers.test.topic=easyevent
easyevent.transfer.trigger.rocketmq.consumers.test.consume-concurrently-max-span=10
easyevent.transfer.trigger.rocketmq.consumers.test.tags=*
easyevent.transfer.trigger.rocketmq.consumers.test.consumer-min-thread=1
easyevent.transfer.trigger.rocketmq.consumers.test.consumer-max-thread=3
easyevent.transfer.trigger.rocketmq.consumers.test.consume-max-retry=5
easyevent.transfer.trigger.rocketmq.consumers.test.consume-retry-delay-time-interval-seconds=5
easyevent.transfer.trigger.rocketmq.consumers.test.consume-liming-retry-delay-time-base-seconds=5
```

### Rate Limiting

To ensure system stability, EasyEvent provides rate limiting before sending and consuming events.  
Users can configure different rate limits as needed. If rate limiting is not passed, an exception [com.openquartz.easyevent.transfer.api.limiting.LimitingBlockedException](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/easyevent-transfer/easyevent-transfer-api/src/main/java/com/openquartz/easyevent/transfer/api/limiting/LimitingBlockedException.java#L10-L16) should be thrown.

#### Sender-Side Rate Limiting

Rate limiting extension point before sending messages: Interface [com.openquartz.easyevent.transfer.api.limiting.EventTransferSenderLimitingControl](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/easyevent-transfer/easyevent-transfer-api/src/main/java/com/openquartz/easyevent/transfer/api/limiting/EventTransferSenderLimitingControl.java#L11-L33).

```java
package com.openquartz.easyevent.transfer.api.limiting;

import java.util.List;
import java.util.function.BiConsumer;
import com.openquartz.easyevent.storage.identify.EventId;

/**
 * EventTransfer Sender Rate Limiting Control
 *
 * @author svnee
 */
public interface EventTransferSenderLimitingControl {

    /**
     * Control event handling
     * Throw {@link LimitingBlockedException} if rate limited
     *
     * @param event Event content
     * @param eventId Event ID
     * @param senderConsumer Sender function
     */
    <T> void control(T event, EventId eventId, BiConsumer<T, EventId> senderConsumer);

    /**
     * Control batch event handling
     * Throw {@link LimitingBlockedException} if rate limited
     *
     * @param eventList List of events
     * @param eventIdList List of event IDs
     * @param batchSenderConsumer Batch sender function
     */
    <T> void control(List<T> eventList, List<EventId> eventIdList,
        BiConsumer<List<T>, List<EventId>> batchSenderConsumer);
}
```

Default implementation: [com.openquartz.easyevent.transfer.api.limiting.impl.DefaultEventTransferSenderLimitingControl](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/easyevent-transfer/easyevent-transfer-api/src/main/java/com/openquartz/easyevent/transfer/api/limiting/impl/DefaultEventTransferSenderLimitingControl.java#L13-L26)

#### Trigger-Side Rate Limiting

Rate limiting extension point before consuming messages: Interface [com.openquartz.easyevent.transfer.api.limiting.EventTransferTriggerLimitingControl](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/easyevent-transfer/easyevent-transfer-api/src/main/java/com/openquartz/easyevent/transfer/api/limiting/EventTransferTriggerLimitingControl.java#L10-L21).

```java
package com.openquartz.easyevent.transfer.api.limiting;

import java.util.function.Consumer;
import com.openquartz.easyevent.transfer.api.message.EventMessage;

/**
 * EventTransfer Trigger Rate Limiting Control
 *
 * @author svnee
 */
public interface EventTransferTriggerLimitingControl {

    /**
     * Control
     * Throw {@link LimitingBlockedException} if rate limited
     *
     * @param eventMessage Event message
     * @param eventHandleFunction Handling function
     */
    void control(EventMessage eventMessage, Consumer<EventMessage> eventHandleFunction);
}
```

Default implementation: [com.openquartz.easyevent.transfer.api.limiting.impl.DefaultEventTransferTriggerLimitingControl](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/easyevent-transfer/easyevent-transfer-api/src/main/java/com/openquartz/easyevent/transfer/api/limiting/impl/DefaultEventTransferTriggerLimitingControl.java#L11-L17)

### Distributed Locks

Since EasyEvent uses middleware for distributed scheduling, there may be cases where messages are lost, triggering fails, or backpressure occurs. Therefore, EasyEvent includes compensatory job triggers.  
It is difficult to guarantee that the same event will not be consumed concurrently at the same time. Currently, EasyEvent provides single-node safety. In a distributed environment, users need to implement a custom distributed lock to ensure concurrency control, or handle concurrency within the subscriber's actual event handling logic.

For implementing distributed locks, the system provides the extension interface [com.openquartz.easyevent.common.concurrent.lock.DistributedLockFactory](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/easyevent-common/src/main/java/com/openquartz/easyevent/common/concurrent/lock/DistributedLockFactory.java#L10-L20).

```java
package com.openquartz.easyevent.common.concurrent.lock;

import java.util.concurrent.locks.Lock;
import com.openquartz.easyevent.common.model.Pair;

/**
 * Distributed Event Lock
 *
 * @author svnee
 */
public interface DistributedLockFactory {

    /**
     * Get Lock
     *
     * @param lockKey Lock key
     * @return Lock instance, must not be null
     */
    Lock getLock(Pair<String, LockBizType> lockKey);
}
```

Users can implement this interface using third-party distributed middleware and inject it into the Spring factory.  
It is recommended to use `Redisson` as the distributed lock implementation.

### Distributed ID Generation

When using JDBC-based `EventStorage`, [EventId](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/easyevent-storage/easyevent-storage-api/src/main/java/com/openquartz/easyevent/storage/identify/EventId.java#L7-L27) provides a default implementation based on database auto-increment.  
If other ID generation strategies are required, users can customize by implementing the interface [com.openquartz.easyevent.storage.identify.IdGenerator](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/easyevent-storage/easyevent-storage-api/src/main/java/com/openquartz/easyevent/storage/identify/IdGenerator.java#L7-L21).

```java
package com.openquartz.easyevent.storage.identify;

/**
 * ID Generator
 *
 * @author svnee
 **/
public interface IdGenerator {

    /**
     * Generate ID
     * Return null means use database auto-increment
     *
     * @return Generated ID
     */
    default Long generateId() {
        return null;
    }
}
```

And then inject it into the Spring factory.  
It is recommended to use the Snowflake algorithm for ID generation.

### Table Sharding Support

EasyEvent supports custom table sharding based on `EventEntityID`. By default, hash-based sharding is used.  
Custom sharding routing interface: [com.openquartz.easyevent.storage.jdbc.sharding.ShardingRouter](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/easyevent-storage/easyevent-storage-jdbc/src/main/java/com/openquartz/easyevent/storage/jdbc/sharding/ShardingRouter.java#L7-L24).  
Default implementation: [com.openquartz.easyevent.storage.jdbc.sharding.impl.DefaultShardingRouterImpl](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/easyevent-storage/easyevent-storage-jdbc/src/main/java/com/openquartz/easyevent/storage/jdbc/sharding/impl/DefaultShardingRouterImpl.java#L14-L40). Depends on providing an [IdGenerator](file:///Users/jackxu/Documents/Code/github.com/openquartz/easy-event/easyevent-storage/easyevent-storage-api/src/main/java/com/openquartz/easyevent/storage/identify/IdGenerator.java#L7-L21) implementation.

```java
package com.openquartz.easyevent.storage.jdbc.sharding;

/**
 * Sharding Router
 *
 * @author svnee
 */
public interface ShardingRouter {

    /**
     * Shard the given entity ID
     *
     * If sharding is disabled, return a value less than 0. Otherwise, return the shard index.
     *
     * @param eventEntityId Entity ID
     * @return Shard index
     */
    int sharding(Long eventEntityId);

    /**
     * Get total number of shards
     * @return Total number of shards
     */
    int totalSharding();
}
```