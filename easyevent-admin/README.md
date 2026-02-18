# EasyEvent Admin Module

EasyEvent Admin is a management console for the EasyEvent framework, providing event monitoring, manual retry, and statistics.

## Features
- **Event Management**: Pagination, filtering (Sharding Key, Event Key, State, Time).
- **Manual Retry**: Reset event state to trigger compensation retry.
- **Dashboard**: Real-time statistics, success rate, latency ranking.
- **Docker Support**: Easy deployment via Docker Compose.

## Tech Stack
- **Backend**: Spring Boot, Spring JDBC (JdbcTemplate), MySQL.
- **Frontend**: Vue 3, TypeScript, Element Plus, ECharts, Vite.

## Prerequisite
- Java 8+
- Maven 3.x
- Node.js 18+ (for frontend development)
- MySQL 5.7+

## Build & Run

### 1. Build Frontend
```bash
cd easyevent-admin-ui
npm install
npm run build
```
This will generate static files into `../easyevent-admin-server/src/main/resources/static`.

### 2. Build Backend
```bash
# In project root
mvn clean package -pl easyevent-admin -am -DskipTests
```
Or go to `easyevent-admin` directory and run:
```bash
mvn clean package -DskipTests
```

### 3. Run Locally
```bash
java -jar easyevent-admin-server/target/easyevent-admin-server-1.5.0-beta.jar
```
Access: http://localhost:8088

### 4. Run with Docker Compose
```bash
cd easyevent-admin-server
docker-compose up -d
```

## Configuration
Update `application.yml` or pass environment variables:
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
