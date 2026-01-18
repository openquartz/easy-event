# 快速开始指南

## 1. 本地安装

要在本地构建和安装项目，请在项目根目录下执行以下命令：

```bash
# 首先安装公共模块
cd easyevent-common
mvn clean install

# 安装父 pom
cd ../easyevent-spring-boot-starter/easyevent-spring-boot-starter-parent
mvn clean install

# 安装存储模块
cd ../../easyevent-storage/easyevent-storage-api
mvn clean install
cd ../easyevent-storage-jdbc
mvn clean install

# 安装传输模块 (可选，按需安装)
cd ../../easyevent-transfer/easyevent-transfer-api
mvn clean install
cd ../easyevent-transfer-disruptor
mvn clean install
# ... 其他传输模块

# 安装核心模块
cd ../../easyevent-core
mvn clean install

# 安装 starter
cd ../easyevent-spring-boot-starter
mvn clean install
```

## 2. 引入依赖

在你的 Spring Boot 项目的 `pom.xml` 中添加 `easy-event` 依赖：

```xml
<dependency>
    <groupId>com.openquartz</groupId>
    <artifactId>easyevent-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 3. 应用配置

在 `application.yml` 中配置数据库和其他设置：

```yaml
server:
  port: 8080

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/easy_event?useUnicode=true&characterEncoding=utf-8&useSSL=false
    username: root
    password: password
```

## 4. 定义事件

创建一个继承自 `BaseEventEntity` 的事件类：

```java
public class UserRegisteredEvent extends BaseEventEntity {
    private Long userId;
    private String username;

    // Getters, Setters, Constructor
    public UserRegisteredEvent(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }
    // ...
}
```

## 5. 发布事件

注入 `EventPublisher` 并发布事件：

```java
@Service
public class UserService {
    @Resource
    private EventPublisher eventPublisher;

    public void registerUser(String username) {
        // 业务逻辑...
        Long userId = saveUser(username);

        // 发布事件
        eventPublisher.publish(new UserRegisteredEvent(userId, username));
    }
}
```

## 6. 订阅事件

创建监听器 Bean 并使用 `@Subscribe` 注解：

```java
@Component
public class UserEventListener {

    @Subscribe
    @AllowConcurrentEvents // 可选：允许并发执行
    public void onUserRegistered(UserRegisteredEvent event) {
        System.out.println("用户注册: " + event.getUsername());
        // 处理事件...
    }
}
```
