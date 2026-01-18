<div align=center><img src="/doc/image/logo.png" width="200"/></div>

<div align=center>

# EasyEvent

**Make Distributed Event Driven Easier**
<br>
让分布式事件驱动开发更简单、更可靠

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/com.openquartz/easy-event.svg)](https://search.maven.org/search?q=g:com.openquartz%20a:easy-event)
[![Build Status](https://github.com/openquartz/easy-event/actions/workflows/ci.yml/badge.svg)](https://github.com/openquartz/easy-event/actions)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/openquartz/easy-event/pulls)

[English](README_en.md) | [中文简体](README.md)

</div>

---

## 📖 简介 | Introduction

`EasyEvent` 是一款基于 **Google Guava EventBus** 思想设计的**分布式、高可靠、最终一致性**的实时事件总线框架。

它保留了 Guava EventBus 简单易用的编程模型，同时解决了其**不支持持久化**、**无法跨机器调度**、**缺乏异常补偿机制**等痛点，致力于为微服务架构提供轻量级、高性能的事件驱动解决方案。

## ✨ 核心特性 | Features

- **🛡️ 高可靠性 (Reliability)**
    - 采用“本地消息表 + 最终一致性”模式，确保事件 100% 不丢失。
    - 支持发布者/消费者双重确认机制。

- **⚡ 高性能 (High Performance)**
    - **Fire-and-Forget**: 支持非阻塞异步投递模式，提升高并发场景下的吞吐量。
    - **Batch Insert**: 集成 Snowflake 算法，支持高效的 JDBC 批量事件写入。

- **🔍 全链路追踪 (Distributed Tracing)**
    - 内置 `TraceContext`，支持跨线程、跨服务的全链路 Trace ID 透传。
    - 完美适配日志监控系统，快速定位分布式环境下的问题。

- **🔌 插件化架构 (Pluggable Architecture)**
    - **存储层**: 支持 JDBC (MySQL/PostgreSQL) 等关系型数据库，易于扩展。
    - **传输层**: 支持 Kafka, RocketMQ, Disruptor, RabbitMQ 等多种中间件，按需切换。

- **⚖️ 灵活隔离 (Flexible Isolation)**
    - 支持基于 Tag 的事件隔离。
    - 独立的线程池管理，防止单一业务阻塞导致系统雪崩。

- **🔄 自动补偿 (Auto Compensation)**
    - 内置失败重试与补偿机制，智能处理异常情况下的事件恢复。

## 🏗️ 架构概览 | Architecture

EasyEvent 通过抽象 `EventStorage` (存储) 和 `EventTransfer` (传输) 将事件的生命周期解耦，实现了灵活的分布式调度。

**单机处理流程：**
![EasyEvent Architecture](./doc/image/EasyEvent.png)

**SOA/分布式处理流程：**
![SOAEvent Architecture](./doc/image/SOAEvent.png)

## 🚀 快速开始 | Quick Start

### 1. 引入依赖 (Maven)

在 `pom.xml` 中添加核心依赖（以 Disruptor 传输层为例）：

```xml
<properties>
    <easyevent.version>1.5.0-beta</easyevent.version>
</properties>

<dependency>
    <groupId>com.openquartz</groupId>
    <artifactId>easyevent-spring-boot-starter-parent</artifactId>
    <version>${easyevent.version}</version>
</dependency>
<dependency>
    <groupId>com.openquartz</groupId>
    <artifactId>easyevent-spring-boot-starter-disruptor</artifactId>
    <version>${easyevent.version}</version>
</dependency>
```

### 2. 定义事件 (Define Event)

```java
public class UserRegisteredEvent {
    private Long userId;
    private String username;

    public UserRegisteredEvent(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }
    // Getters and Setters...
}
```

### 3. 发布事件 (Publish Event)

注入 `EventBus` 并发布事件：

```java
@Service
public class UserService {
    @Autowired
    private EventBus eventBus;

    public void registerUser(User user) {
        // 业务逻辑...
        // 发布事件
        eventBus.post(new UserRegisteredEvent(user.getId(), user.getName()));
    }
}
```

### 4. 订阅事件 (Subscribe Event)

实现事件处理器并添加 `@Subscribe` 注解：

```java
@Component
public class UserEventHandler {

    @Subscribe
    @AllowConcurrentEvents // 开启并行处理
    public void onUserRegistered(UserRegisteredEvent event) {
        System.out.println("User registered: " + event.getUsername());
    }
}
```

## 📊 同类对比 | Comparison

| 特性 | EasyEvent | Google EventBus | Spring Cloud Bus | RocketMQ EventBridge |
|---|---|---|---|---|
| **定位** | 分布式、持久化、最终一致性 | 单机内存 | 配置刷新/消息总线 | 云原生事件桥接 |
| **分布式支持** | ✅ 支持 (多机/SOA) | ❌ 仅单机 | ✅ 支持 | ✅ 支持 |
| **持久化** | ✅ 本地消息表 (高可靠) | ❌ 无 | ❌ 无 | ✅ 依赖 MQ |
| **运维成本** | ⭐ 低 (DB + 任意MQ) | ⭐ 无 | ⭐⭐ 低 | ⭐⭐⭐ 中 |
| **链路追踪** | ✅ 内置支持 | ❌ 无 | ❌ 需集成 Sleuth | ✅ 依赖云厂商 |
| **隔离性** | ✅ 线程池/Tag隔离 | ❌ 无 | ❌ 无 | ❌ 无 |

## 🔗 更多文档 | Documentation

- [快速开始指南](doc/QuickStart_zh.md)
- [扩展开发指南](doc/Extend_zh.md)

## 🤝 贡献 | Contribution

欢迎提交 **Issue** 和 **Pull Request**！如果您觉得本项目对您有帮助，请给一个 ⭐️ **Star** 支持一下！

## 📄 许可证 | License

[Apache 2.0 License](LICENSE)

---

<div align="center">
    
[![Star History Chart](https://api.star-history.com/svg?repos=openquartz/easy-event&type=Date)](https://star-history.com/#openquartz/easy-event&Date)

</div>
