# 扩展指南

## 1. 拦截器扩展

EasyEvent 提供了事件处理三个阶段的拦截能力：**发布前后**、**触发前后**、**处理前后**。您可以根据需要实现自定义逻辑进行统一拦截。

### 1.1 发布拦截 (Publisher Interception)

在调用 `EventPublisher` 发布事件的前后进行拦截。

接口：`com.openquartz.easyevent.core.intreceptor.PublisherInterceptor`

```java
public interface PublisherInterceptor {
    /**
     * 发布前钩子
     * @param event 发布的事件
     */
    void onPublish(Object event);

    /**
     * 发布后钩子
     * @param event 发布的事件
     */
    void afterPublish(Object event);
}
```

实现该接口并注册为 Spring Bean 即可生效。

### 1.2 触发拦截 (Trigger Interception)

在异步任务被触发（例如提交到线程池或消息队列）的前后进行拦截。

接口：`com.openquartz.easyevent.core.intreceptor.TriggerInterceptor`

```java
public interface TriggerInterceptor {
    /**
     * 触发前钩子
     * @param eventContext 事件上下文
     */
    void onTrigger(EventContext eventContext);

    /**
     * 触发后钩子
     * @param eventContext 事件上下文
     */
    void afterTrigger(EventContext eventContext);
}
```

### 1.3 处理拦截 (Handler Interception)

在订阅者方法实际执行的前后进行拦截。

接口：`com.openquartz.easyevent.core.intreceptor.HandlerInterceptor`

```java
public interface HandlerInterceptor {
    /**
     * 处理前钩子
     * @param eventContext 事件上下文
     */
    void onHandle(EventContext eventContext);

    /**
     * 处理后钩子
     * @param eventContext 事件上下文
     */
    void afterHandle(EventContext eventContext);
}
```

## 2. 事件路由 (Event Routing)

您可以自定义事件路由策略，决定事件的分发路径。

接口：`com.openquartz.easyevent.core.route.EventRouter`

```java
public interface EventRouter {
    /**
     * 决定事件的路由键
     * @param event 事件对象
     * @return 路由键
     */
    String route(Object event);
}
```

默认实现：`DefaultEventRouter`（基于事件类型路由）。

## 3. 限流控制 (Rate Limiting)

EasyEvent 支持在 **发送** 和 **触发** 阶段进行限流。

### 3.1 传输发送限流

控制事件发送到传输层（如 Disruptor, Kafka）的速率。

接口：`com.openquartz.easyevent.core.limiting.EventTransferSenderLimitingControl`

### 3.2 传输触发限流

控制消费者处理事件的速率。

接口：`com.openquartz.easyevent.core.limiting.EventTransferTriggerLimitingControl`

## 4. 分布式锁 (Distributed Lock)

在集群环境下，某些协调任务需要分布式锁支持。

接口：`com.openquartz.easyevent.common.lock.DistributedLockFactory`

```java
public interface DistributedLockFactory {
    /**
     * 获取分布式锁
     * @param key 锁键
     * @return 锁实例
     */
    Lock getLock(String key);
}
```

用户需要实现此接口（例如使用 Redis/Redisson 或 Zookeeper）并注册为 Spring Bean。

## 5. 分布式 ID 生成

自定义事件 ID 的生成策略。

接口：`com.openquartz.easyevent.storage.identify.IdGenerator`

```java
public interface IdGenerator {
    /**
     * 生成唯一 ID
     * @return 唯一 ID
     */
    Long generateId();
}
```

默认实现使用雪花算法 (Snowflake)。

## 6. 分表策略 (Table Sharding)

如果事件数据量较大，可以实现分表策略。

接口：`com.openquartz.easyevent.storage.sharding.ShardingRouter`

```java
public interface ShardingRouter {
    /**
     * 决定表下标
     * @param event 事件对象
     * @return 表下标后缀
     */
    int route(BaseEventEntity event);
}
```