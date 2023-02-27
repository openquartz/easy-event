## 扩展

### 拦截

EasyEvent 针对事件的拦截,提供了三个节点的拦截。分别为 **发布前后**、**触发前后**、**处理前后**。用户可以根据自己的需要做对应的实现进行统一的拦截处理。

#### 发布前后拦截

服务提供 在调用`org.svnee.easyevent.core.publisher.EventPublisher`时发布前完成时，发布前后进行拦截。\
拦截接口为：`org.svnee.easyevent.core.intreceptor.PublisherInterceptor`并且注入到Spring 工厂中。

```java
package org.svnee.easyevent.core.intreceptor;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * 同步拦截器
 *
 * @author svnee
 */
public interface PublisherInterceptor {

    /**
     * 默认先拦截顺序
     *
     * @return 顺序
     */
    default int order() {
        return Integer.MAX_VALUE;
    }

    /**
     * 发布开始之前
     *
     * @param event event
     * @param context 上下文
     * @return true-执行下一个拦截器，否则默认已经响应完成。直接返回
     */
    default boolean prePublish(Object event, PublisherInterceptorContext context) {
        return true;
    }

    /**
     * 发布完成后
     *
     * @param event event
     * @param context context
     * @param ex 发生异常时的异常信息
     */
    default void afterCompletion(Object event, PublisherInterceptorContext context, @Nullable Exception ex) {

    }
}
```

#### 触发前后拦截

在异步发布事件后通过`EventTrigger`时进行前后拦截。提供拦截接口`org.svnee.easyevent.core.intreceptor.TriggerInterceptor`

```java
package org.svnee.easyevent.core.intreceptor;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.svnee.easyevent.transfer.api.message.EventMessage;

/**
 * Trigger Interceptor
 *
 * @author svnee
 */
public interface TriggerInterceptor {

    /**
     * 默认先拦截顺序
     *
     * @return 顺序
     */
    default int order() {
        return Integer.MAX_VALUE;
    }

    /**
     * 处理开始之前
     *
     * @param message trigger-消息
     * @param context context
     * @return trigger flag
     */
    default boolean preTrigger(EventMessage message, TriggerInterceptorContext context) {
        return true;
    }

    /**
     * 处理完成后
     *
     * @param message message
     * @param context context
     * @param ex 发生异常时的异常信息
     */
    default void afterCompletion(EventMessage message, TriggerInterceptorContext context, @Nullable Exception ex) {
    }
}
```

#### 处理前后拦截

事件触发调用订阅者执行业务逻辑前后执行。提供拦截接口`org.svnee.easyevent.core.intreceptor.HandlerInterceptor`

```java
package org.svnee.easyevent.core.intreceptor;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Handle Interceptor
 *
 * @author svnee
 */
public interface HandlerInterceptor<T> {

    /**
     * 默认先拦截顺序
     *
     * @return 顺序
     */
    default int order() {
        return Integer.MAX_VALUE;
    }

    /**
     * 处理开始之前
     *
     * @param event event
     * @param handler 处理器
     * @param context 上下文
     * @return true-执行下一个拦截器，否则默认已经响应完成。直接返回
     */
    default boolean preHandle(T event, Object handler, HandlerInterceptorContext context) {
        return true;
    }

    /**
     * 处理完成后
     *
     * @param event event
     * @param handler 处理器
     * @param context context
     * @param ex 发生异常时的异常信息
     */
    default void afterCompletion(T event, Object handler, HandlerInterceptorContext context,
        @Nullable Exception ex) {
    }
}
```

### 路由

EasyEvent支持用户自定义异步事件发布到不同的队列topic中。默认发布到配置的topic中.配置为:`easyevent.transfer.common.default-topic`
如果用户需要将不同的消息发送到不同的队列的topic中时。可以实现接口`org.svnee.easyevent.transfer.api.route.EventRouter`

```java
package org.svnee.easyevent.transfer.api.route;

import org.svnee.easyevent.common.model.Pair;

/**
 * 事件路由服务
 *
 * @author svnee
 */
public interface EventRouter {

    /**
     * 事件路由topic
     *
     * @param event event
     * @return 路由topic。key: topic,value: 和具体实现相关。如果是 rocketmq指向tag,kafka指向partition.可为null
     */
    Pair<String, String> route(Object event);
}
```

如果实现了自定义路由需要在配置中添加消费者配置。
`easyevent.transfer.trigger.<mq-alias>.consumers.<consumer-alias>.<property>`

例如：

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

### 限流

针对系统的稳定性这一块,EasyEvent服务提供了在事件发送前后的限流。\
用户可以根据需要设置不同的限流。如果限流不通过需要抛出异常`org.svnee.easyevent.transfer.api.limiting.LimitingBlockedException`

#### 发送前限流

在发送消息前提供限流扩展点,接口为:`org.svnee.easyevent.transfer.api.limiting.EventTransferSenderLimitingControl`

```java
package org.svnee.easyevent.transfer.api.limiting;

import java.util.List;
import java.util.function.BiConsumer;
import org.svnee.easyevent.storage.identify.EventId;

/**
 * EventTransfer Sender Limiting Control
 *
 * @author svnee
 */
public interface EventTransferSenderLimitingControl {

    /**
     * control event handle
     * if limiting blocked throw {@link LimitingBlockedException}
     *
     * @param event event content
     * @param eventId eventId
     * @param senderConsumer sender function
     */
    <T> void control(T event, EventId eventId, BiConsumer<T, EventId> senderConsumer);

    /**
     * control event handle
     * if limiting blocked throw {@link LimitingBlockedException}
     *
     * @param eventList eventList
     * @param eventIdList eventIdList
     * @param batchSenderConsumer batch sender function
     */
    <T> void control(List<T> eventList, List<EventId> eventIdList,
        BiConsumer<List<T>, List<EventId>> batchSenderConsumer);
}
```

默认实现为：`org.svnee.easyevent.transfer.api.limiting.impl.DefaultEventTransferSenderLimitingControl`

#### 触发前限流

在消费消息前提供限流扩展点,接口为:`org.svnee.easyevent.transfer.api.limiting.EventTransferTriggerLimitingControl`

```java
package org.svnee.easyevent.transfer.api.limiting;

import java.util.function.Consumer;
import org.svnee.easyevent.transfer.api.message.EventMessage;

/**
 * EventTransfer Trigger Limiting Control
 *
 * @author svnee
 */
public interface EventTransferTriggerLimitingControl {

    /**
     * control
     * if limiting blocked throw {@link LimitingBlockedException}
     *
     * @param eventMessage event-message
     * @param eventHandleFunction function
     */
    void control(EventMessage eventMessage, Consumer<EventMessage> eventHandleFunction);
}
```

默认实现为: `org.svnee.easyevent.transfer.api.limiting.impl.DefaultEventTransferTriggerLimitingControl`

### 分布式锁

由于`EasyEvent`需要中间件来做分布式调度时,可能存在消息丢失的情况或者触发失败以及积压等情况时,所以`EasyEvent`设置了补偿Job触发。\
所以很难保证在同一时刻的同一事件的消费不会并发执行。目前`EasyEvent`提供了单机的安全。但是在分布式环境下，需要用户自定义实现分布式锁以保证并发。 或者用户在 消费订阅者的 实际事件处理逻辑中兼容掉此并发。

如果用户实现分布式锁的，系统提供扩展点接口`org.svnee.easyevent.common.concurrent.lock.DistributedLockSupport`

```java
package org.svnee.easyevent.common.concurrent.lock;

import java.util.concurrent.locks.Lock;
import org.svnee.easyevent.common.model.Pair;

/**
 * Distributed EventLock
 *
 * @author svnee
 */
public interface DistributedLockSupport {

    /**
     * Get Lock
     *
     * @param lockKey lockKey
     * @return lock must not be null
     */
    Lock getLock(Pair<String, LockBizType> lockKey);
}
```

用户可以使用第三方分布式中间件实现此接口,并注入到Spring工厂中。\
推荐使用`Redisson`作为分布式锁的实现

### 分布式ID

`EasyEvent`在使用EventStorage存储时。使用jdbc作为实现时,`EventId`提供了默认基于数据库ID的实现方案。\
如果需要使用其他的作为实现EventId, 用户可以自定义实现接口:`org.svnee.easyevent.storage.identify.IdGenerator`

```java
package org.svnee.easyevent.storage.identify;

/**
 * ID 生成 器
 *
 * @author svnee
 **/
public interface IdGenerator {

    /**
     * 生成ID
     * 如果返回null 代表使用数据库自增实现
     *
     * @return ID
     */
    default Long generateId() {
        return null;
    }
}
```

并注入到Spring工厂中。\
推荐使用`雪花算法ID`

### 分表支持

`EasyEvent`支持按照EventEntityID 进行自定义分表。默认是进行hash 分表。
自定义分表路由接口为:`org.svnee.easyevent.storage.jdbc.sharding.ShardingRouter`.

默认实现为：`org.svnee.easyevent.storage.jdbc.sharding.impl.DefaultShardingRouterImpl`.依赖提供`IdGenerator`的实现。
```java
package org.svnee.easyevent.storage.jdbc.sharding;

/**
 * sharding
 *
 * @author svnee
 */
public interface ShardingRouter {

    /**
     * 分片
     *
     * 如果不开启分片时,需要返回值小于0即可。否则返回的是下标
     *
     * @param eventEntityId entityId
     * @return sharding index
     */
    int sharding(Long eventEntityId);

    /**
     * totalSharding
     * @return totalSharding
     */
    int totalSharding();
}
```