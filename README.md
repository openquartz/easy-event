<div align=center><img src="/doc/image/logo.png" width="200"/></div>

<div align=center> Make Distributed Event Driven Easier !!! </div>

# EasyEvent

> **Note**: This project has not been published to the Maven central repository. You need to manually add it to your local or private repository for use.

### Welcome to Star (Welcome Star)!!!

**[Home Page](https://openquartz.github.io/)** \
**[GitHub](https://github.com/openquartz/easy-event)**

### Introduction

#### What is EasyEvent

`EasyEvent` is a distributed, extensible, and eventually consistent real-time EventBus solution based on `Google Guava`'s EventBus.  
It mainly addresses issues such as lack of persistence and distributed machine scheduling in the `Google EventBus`.

#### Features

- Supports execution of synchronous events, asynchronous events, serial events/parallel events triggering.
- Supports event exception compensation and warning functions.

#### Problems Solved

1. Solves data consistency problems in event-driven architectures.
2. Addresses service instability caused by concentrated event triggers.
3. Resolves distributed event traceability issues.
4. Handles monitoring and early-warning for abnormal event executions.
5. Addresses event-driven challenges in Domain-Driven Design (DDD).
6. Solves various distributed event issues: e.g., excessive storage demands for events, sharp traffic spikes due to clustered events, slow processing blocking other normal event subscriptions, etc.

### Software Architecture

To address these issues, `EasyEvent` abstracts several core components. It uses `EventStorage`, which can be extended via SPI implementations.  
For distributed event scheduling, it utilizes `EventTransfer`, also extendable through custom SPI implementations.  
The asynchronous event processing workflow is illustrated below:

**Diagram for Single-node EasyEvent Processing:**  
![EasyEvent Asynchronous Event Processing Flow Chart](./doc/image/EasyEvent.png)

**Diagram for SOAEvent Processing:**  
![SOAEvent Asynchronous Event Processing Flow Chart](./doc/image/SOAEvent.png)

#### Code Structure

- `easyevent-common`: Public module service

- `easyevent-core`: Core EventBus logic

- `easyevent-storage`: Storage Service
    - `easyevent-storage-api`: Storage Service API
    - `easyevent-storage-jdbc`: JDBC-based storage implementation

- `easyevent-transfer`: Event Transfer Service
    - `easyevent-transfer-api`: Event transfer protocol
    - `easyevent-transfer-disruptor`: Disruptor as an event transfer implementation
    - `easyevent-transfer-rocketmq`: RocketMQ as an event transfer implementation
    - `easyevent-transfer-kafka`: Kafka as an event transfer implementation

- `easyevent-spring-boot-starter`: EasyEvent Starter Package
    - `easyevent-spring-boot-starter-parent`: `easyevent starter parent` project
    - `easyevent-spring-boot-starter-disruptor`: Spring Boot starter with Disruptor as the event transfer implementation
    - `easyevent-spring-boot-starter-kafka`: Spring Boot starter with Kafka as the event transfer implementation
    - `easyevent-spring-boot-starter-rocketmq`: Spring Boot starter with RocketMQ as the event transfer implementation

- `easyevent-spring-boot-starter-soa`: EasyEvent SOA Support Starter Package
    - `easyevent-spring-boot-starter-soa-api`: `easyevent soa event api` project
    - `easyevent-spring-boot-starter-soa-parent`: `easyevent soa starter parent` project
    - `easyevent-spring-boot-starter-soa-rocketmq`: SOA-based event transfer center using RocketMQ as the implementation

- `easyevent-example`: Example Projects
    - `easyevent-example-disruptor`: Uses Disruptor as the transport scheduling layer
    - `easyevent-example-rocketmq`: Uses RocketMQ as the transport scheduling layer
    - `easyevent-example-kafka`: Uses Kafka as the transport scheduling layer
    - `easyevent-example-soa`: SOA Example Project
        - `easyevent-example-soa-event`: SOA public events
        - `easyevent-example-soa-publisher`: SOA event publishing service
        - `easyevent-example-soa-subscriber`: SOA event subscription service

### Quick Start

[Quick Start Guide](doc/QuickStart.md)

### Extensibility

[Extending EasyEvent](doc/Extend.md)

### Comparison with Similar Frameworks

There are many event-driven frameworks available, including `Google EventBus`, `SpringCloud-Bus`, `killbill-queue`, and `RocketMQ EventBridge`.  
Below is a comparison focused on each framework's key features:

|     | easy-event                                                      | Google EventBus           | killbill-queue                        | SpringCloud-Bus                           | RocketMQ EventBridge                              |
|-----|-----------------------------------------------------------------|---------------------------|---------------------------------------|-------------------------------------------|---------------------------------------------------|
| Description  | A distributed, eventually consistent EventBus solution based on `Google Guava`, with similar usage | In-memory EventBus based on single machine | Extended based on `DB` + `Google EventBus` | Provides both standalone and distributed EventBus for SpringCloud | High-reliability, low-coupling, high-performance event-driven architecture based on `RocketMQ 5.0` |
| Distributed Support | Supports distributed environments across multiple machines in a single service, SOA microservices | Single-machine only         | Supports distributed environments for a single service | Supports distributed environments in microservices | Supports distributed environments in microservices |
| Reliability | Uses local message tables to ensure no data loss in distributed event-driven scenarios, achieving eventual consistency | Events not persisted, potential for loss | Uses local message tables for distributed event-driven handling | Not persistent; reliability depends on MQ in distributed mode | Depends on the stability of `RocketMQ` |
| Ease of Use | Provides a `SpringBootStarter` package; simply introduce dependencies for quick adoption. Usage is nearly identical to `Google EventBus` | Simple to use             | Same usage as `Google EventBus`          | Usage similar to `Spring Application Event` | Introduce EventBridge-related dependencies |
| Operations & Maintenance | Common component ecosystem, no additional deployment required (`DB` + `MQ`) | No additional deployment needed | Depends on Oracle (no MySQL version available) | Common component ecosystem, no additional deployment required (depends on MQ) | Deployment requires both `RocketMQ EventBridge` and `RocketMQ` |
| Isolation   | Supports customizable isolation (storage, transmission) for different events with extensibility | No isolation                | No isolation                             | No isolation                                | No isolation                                      |
| Stability   | Asynchronous event triggering supports custom rate limiting for both consumption and sending; tag-level isolation supported | No rate control             | Timed scheduling, difficult to control frequency, poor real-time performance | In distributed environments, heavily dependent on MQ stability; sudden spikes in events can easily cause bottlenecks | Depends on the stability of `RocketMQ EventBridge` and `RocketMQ` |
| Extensibility | Storage supports relational databases like `JDBC`, and can be customized via APIs. Transmission supports `RocketMQ`, `Kafka`, and can also be extended | Not extensible            | Depends on `Oracle`; other storage components require code rewrites | Multiple transmission components provided, easy to extend | Strongly dependent on `RocketMQ`; no alternative components currently available |

