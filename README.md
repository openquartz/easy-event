# EasyEvent
**Make Distributed Event Driven Easier!**

> **注意**：本项目未发布到maven中央仓库，需要手动添加到本地仓库 或者 到私有仓库中使用。

### 欢迎Star (Welcome Star)!!!

**[主页](https://svnlab.github.io/)** \
**[Github](https://github.com/openquartz/easy-event)**

### 介绍

#### 什么是EasyEvent

`EasyEvent`是一款基于`Google Guava` 的`EventBus`为蓝本的分布式的,可扩展的,最终一致性的实时的`EventBus`解决方案。\
主要是解决`Google EventBus`的不支持持久化和分布式机器调度等的问题。

#### 功能特性

支持同步事件、异步事件、串行事件/并行事件触发执行。\
支持事件异常补偿、预警等功能

#### 解决问题

1、解决使用事件驱动中的数据一致性问题

2、解决事件集中触发出现时,服务不稳定性问题

3、解决分布式事件追踪性问题

4、解决针对事件执行异常监控预警问题

5、解决DDD中的事件驱动问题

6、解决分布式事件中的问题：例如：事件过多的存储问题。事件集中出现导致尖刺明显。存在处理慢的实现阻塞其他事件正常订阅等等

### 软件架构

`EasyEvent` 为了实现上述问题。抽象出几个核心的角色。通过`EventStorage` 进行存储,可以通过SPI的形式进行扩展实现。\
通过`EventTransfer`进行事件的分布式调度处理。也可通过自定义SPI实现。\
异步事件处理调度如下图所示：

![EasyEvent异步事件处理流程示意图](./doc/image/EasyEvent.png)

#### 代码结构

- `easyevent-common`: 公共模块服务

- `easyevent-core`: 核心eventbus逻辑

- `easyevent-storage`: 存储服务
  - `easyevent-storage-api`: 存储服务`API`
  - `easyevent-storage-jdbc`: 基于`jdbc`的存储实现

- `easyevent-transfer`: 事件传输服务
  - `easyevent-transfer-api`: 事件传输协议
  - `easyevent-transfer-disruptor`: disruptor 作为事件传输的实现
  - `easyevent-transfer-rocketmq`: rocketmq 作为事件传输的实现
  - `easyevent-transfer-kafka`: kafka 作为事件传输的实现

- `easyevent-spring-boot-starter`: easyevent starter 包
  - `easyevent-spring-boot-starter-parent`: `easyevent starter parent`工程
  - `easyevent-spring-boot-starter-disruptor`: `disruptor` 作为事件传输的实现的 springboot starter
  - `easyevent-spring-boot-starter-kafka`: `kafka` 作为事件传输的实现的 springboot starter
  - `easyevent-spring-boot-starter-rocketmq`: `rocketmq` 作为事件传输的实现的 springboot starter
  
- `easyevent-example`: 样例工程
  - `easyevent-example-disruptor`: 使用`disruptor` 作为传输调度层
  - `easyevent-example-rocketmq`: 使用`rocketmq` 作为传输调度层
  - `easyevent-example-kafka`: 使用`kafka` 作为传输调度层

### 快速开始

[快速开始](doc/QuickStart.md)

### 扩展性

[扩展](doc/Extend.md)


### 同类型对比
事件驱动框架有很多。常见的有：`Google EventBus`、`SpringCloud-Bus`、`killbill-queue`、`RocketMQ EventBridge`. 针对每个框架的所专注的点做一些对比。

|     |  easy-event | Google EventBus | killbill-queue | SpringCloud-Bus | RocketMQ EventBridge |
|  ----  | ----  | --- | ---  | --- |  --- |
| 介绍 | 基于`Google Guava`开发的分布式的最终一致性的，用法基本相同 | 基于单机内存的 `EventBus` | 基于`DB`+`Google EventBus`开发的扩展 | springcloud 提供的可单机可分布式的`EventBus` | 基于`RocketMQ5.0`的高可靠、低耦合、高性能的事件驱动架构  |
| 分布式 | 支持单服务的多机器的分布式环境 | 只支持单机 | 支持单服务的多机器的分布式 | 支持微服务下的分布式环境 | 支持微服务下的分布式环境 | 
| 可靠性  | 使用本地消息表完成分布式事件驱动,数据不会丢失.最终一致性实现 | 事件未持久,存在丢失可能 | 使用本地消息表完成分布式事件驱动 | 未作持久化,分布式下依赖 MQ的可靠性 | 依赖`RocketMQ`的稳定性 |
| 易用性 | 提供`SpringBootStarer`包,引入相关依赖即可快熟使用。使用方法几乎和`Google EventBus` 相同 | 用法简单 | `Google EventBus` 相同 | 和`Spring Application Event`使用用法相同 | 引入EventBridge相关依赖 |
| 运维  | 常用组件生态,无需额外部署(`DB`+`MQ`) |  无需额外部署  | 依赖`Oracle`（无`MySql`对应版本） | 常用组件生态,无需额外部署(依赖`MQ`组件) | 部署服务`RocketMQ EventBridge` 与 `RocketMQ` |
| 隔离性 | 支持不同事件自定义隔离（存储、传递）可扩展 | 无 | 无 | 无 | 无 |
| 稳定性 | 异步事件触发支持消费和发送自定义限流。支持tag级别的隔离 | 无 | 定时调度.很难控制频率。实时性较差 | 分布式下事件强依赖MQ稳定性。突发事件过多易出现尖刺 | 依赖服务 `RocketMQ EventBridge` 稳定性和`RocketMQ`稳定性 |
| 扩展性 | 存储支持使用`JDBC`等关系型数据库，也可以根据api自定义扩展。传输支持`RocketMQ`、`Kafka`.也可以自定义扩展 | 无 | 依赖`Oracle`.其他存储组件需要改写代码重新实现 | 传输提供了多个组件的实现。易扩展。 | 强依赖`RocketMQ`,暂未提供其他可替换组件 |


