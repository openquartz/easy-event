# Quick Start Guide

## 1. Installation

To build and install the project locally, execute the following commands in the project root directory:

```bash
# Install common module first
cd easyevent-common
mvn clean install

# Install parent pom
cd ../easyevent-spring-boot-starter/easyevent-spring-boot-starter-parent
mvn clean install

# Install storage modules
cd ../../easyevent-storage/easyevent-storage-api
mvn clean install
cd ../easyevent-storage-jdbc
mvn clean install

# Install transfer modules (optional, install as needed)
cd ../../easyevent-transfer/easyevent-transfer-api
mvn clean install
cd ../easyevent-transfer-disruptor
mvn clean install
# ... other transfer modules

# Install core module
cd ../../easyevent-core
mvn clean install

# Install starter
cd ../easyevent-spring-boot-starter
mvn clean install
```

## 2. Dependency Configuration

Add the `easy-event` dependency to your Spring Boot project's `pom.xml`:

```xml
<dependency>
    <groupId>com.openquartz</groupId>
    <artifactId>easyevent-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 3. Application Configuration

Configure the database and other settings in `application.yml`:

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

## 4. Define an Event

Create an event class extending `BaseEventEntity`:

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

## 5. Publish an Event

Inject `EventPublisher` and publish the event:

```java
@Service
public class UserService {
    @Resource
    private EventPublisher eventPublisher;

    public void registerUser(String username) {
        // Business logic...
        Long userId = saveUser(username);

        // Publish event
        eventPublisher.publish(new UserRegisteredEvent(userId, username));
    }
}
```

## 6. Subscribe to an Event

Create a listener bean and use the `@Subscribe` annotation:

```java
@Component
public class UserEventListener {

    @Subscribe
    @AllowConcurrentEvents // Optional: allow concurrent execution
    public void onUserRegistered(UserRegisteredEvent event) {
        System.out.println("User registered: " + event.getUsername());
        // Handle event...
    }
}
```
