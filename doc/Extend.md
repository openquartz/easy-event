# Extension Guide

## 1. Interception

EasyEvent provides interception at three stages of event processing: **Before/After Publishing**, **Before/After Triggering**, and **Before/After Handling**. You can implement custom logic for unified interception as needed.

### 1.1 Publisher Interception

Interception occurs before and after the event is published via `EventPublisher`.

Interface: `com.openquartz.easyevent.core.intreceptor.PublisherInterceptor`

```java
public interface PublisherInterceptor {
    /**
     * Pre-publish hook
     * @param event The event being published
     */
    void onPublish(Object event);

    /**
     * Post-publish hook
     * @param event The event that was published
     */
    void afterPublish(Object event);
}
```

Implement this interface and register it as a Spring Bean.

### 1.2 Trigger Interception

Interception occurs before the asynchronous task is triggered (e.g., sent to the thread pool or message queue).

Interface: `com.openquartz.easyevent.core.intreceptor.TriggerInterceptor`

```java
public interface TriggerInterceptor {
    /**
     * Pre-trigger hook
     * @param eventContext Event context
     */
    void onTrigger(EventContext eventContext);

    /**
     * Post-trigger hook
     * @param eventContext Event context
     */
    void afterTrigger(EventContext eventContext);
}
```

### 1.3 Handler Interception

Interception occurs before and after the actual subscriber method is invoked.

Interface: `com.openquartz.easyevent.core.intreceptor.HandlerInterceptor`

```java
public interface HandlerInterceptor {
    /**
     * Pre-handle hook
     * @param eventContext Event context
     */
    void onHandle(EventContext eventContext);

    /**
     * Post-handle hook
     * @param eventContext Event context
     */
    void afterHandle(EventContext eventContext);
}
```

## 2. Event Routing

You can customize how events are routed to different processing channels.

Interface: `com.openquartz.easyevent.core.route.EventRouter`

```java
public interface EventRouter {
    /**
     * Determine the route for an event
     * @param event The event
     * @return The route key
     */
    String route(Object event);
}
```

Default implementation: `DefaultEventRouter` (routes based on event type).

## 3. Rate Limiting

EasyEvent supports rate limiting at both the **Send** (Transfer) and **Trigger** stages.

### 3.1 Transfer Sender Limiting

Controls the rate at which events are sent to the transfer layer (e.g., Disruptor, Kafka).

Interface: `com.openquartz.easyevent.core.limiting.EventTransferSenderLimitingControl`

### 3.2 Trigger Limiting

Controls the rate at which events are processed by consumers.

Interface: `com.openquartz.easyevent.core.limiting.EventTransferTriggerLimitingControl`

## 4. Distributed Lock

When using clustering, a distributed lock is required for certain coordination tasks.

Interface: `com.openquartz.easyevent.common.lock.DistributedLockFactory`

```java
public interface DistributedLockFactory {
    /**
     * Get a distributed lock
     * @param key Lock key
     * @return Lock instance
     */
    Lock getLock(String key);
}
```

Users need to implement this interface (e.g., using Redis/Redisson or Zookeeper) and register it as a Spring Bean.

## 5. Distributed ID Generation

Customize how event IDs are generated.

Interface: `com.openquartz.easyevent.storage.identify.IdGenerator`

```java
public interface IdGenerator {
    /**
     * Generate a unique ID
     * @return Unique ID
     */
    Long generateId();
}
```

Default implementation uses Snowflake algorithm.

## 6. Table Sharding

If you have a large volume of events, you can implement table sharding strategies.

Interface: `com.openquartz.easyevent.storage.sharding.ShardingRouter`

```java
public interface ShardingRouter {
    /**
     * Determine the table index
     * @param event The event
     * @return Table index suffix
     */
    int route(BaseEventEntity event);
}
```
