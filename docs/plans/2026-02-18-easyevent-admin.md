# EasyEvent Admin Module Implementation Plan

> **For Trae:** REQUIRED SUB-SKILL: Use `subagent-driven-development` to implement this plan task-by-task.

**Goal:** Design and implement a complete admin management module for `easy-event` including event pagination, manual retry, monitoring, and subscriber management.

**Architecture:** 
- **Backend:** Spring Boot module `easyevent-admin` exposing REST APIs. Connects to `ee_bus_event_entity` table.
- **Frontend:** Vue3 + TypeScript + Element Plus single-page application, built into backend static resources.

**Tech Stack:** Java 8+, Spring Boot, MyBatis Plus, Vue 3, TypeScript, Element Plus, Vite.

---

## Phase 1: Backend Implementation

### Task 1.1: Module Initialization
**Files:**
- Create: `easyevent-admin/pom.xml`
- Modify: `pom.xml` (Root)
- Create: `easyevent-admin/src/main/resources/application.yml`
- Create: `easyevent-admin/src/main/java/com/openquartz/easyevent/admin/AdminApplication.java`

**Steps:**
1. Create `easyevent-admin/pom.xml` with dependencies: `spring-boot-starter-web`, `mybatis-plus-boot-starter`, `mysql-connector-java`, `lombok`.
2. Register module in root `pom.xml`.
3. Create Spring Boot Application class.
4. Create basic configuration.

### Task 1.2: Data Access Layer
**Files:**
- Create: `easyevent-admin/src/main/java/com/openquartz/easyevent/admin/model/BusEventEntity.java`
- Create: `easyevent-admin/src/main/java/com/openquartz/easyevent/admin/mapper/BusEventMapper.java`

**Steps:**
1. Define `BusEventEntity` matching `ee_bus_event_entity` schema.
2. Create MyBatis Plus Mapper interface.

### Task 1.3: Event Management Service & API
**Files:**
- Create: `easyevent-admin/src/main/java/com/openquartz/easyevent/admin/service/EventAdminService.java`
- Create: `easyevent-admin/src/main/java/com/openquartz/easyevent/admin/controller/EventController.java`
- Create: `easyevent-admin/src/main/java/com/openquartz/easyevent/admin/model/query/EventQuery.java`

**Steps:**
1. Implement `EventAdminService` with `pageEvents` method using MyBatis Plus `Page`.
2. Implement `EventController` exposing `/api/events/list`.
3. Support filtering by sharding key, event key, time range, state.

### Task 1.4: Manual Retry Feature
**Files:**
- Modify: `easyevent-admin/src/main/java/com/openquartz/easyevent/admin/service/EventAdminService.java`
- Modify: `easyevent-admin/src/main/java/com/openquartz/easyevent/admin/controller/EventController.java`

**Steps:**
1. Implement `retryEvent(Long eventId)` logic. 
   - Note: Since `EventCompensateService` is in `easyevent-core` and designed for internal use, we might need to replicate the retry logic or depend on `easyevent-core` and use its components if compatible. 
   - *Decision*: Depend on `easyevent-core` to reuse `EventSender` and `AsyncEventHandler`.
2. Add `POST /api/events/retry` endpoint.

### Task 1.5: Monitoring & Statistics
**Files:**
- Create: `easyevent-admin/src/main/java/com/openquartz/easyevent/admin/controller/StatsController.java`
- Modify: `easyevent-admin/src/main/java/com/openquartz/easyevent/admin/mapper/BusEventMapper.java`

**Steps:**
1. Add custom SQL queries in Mapper for stats (count by state, trend over time).
2. Implement `StatsController` exposing `/api/stats/dashboard`.

## Phase 2: Frontend Implementation

### Task 2.1: Project Setup
**Files:**
- Create: `easyevent-admin-ui/package.json`
- Create: `easyevent-admin-ui/vite.config.ts`
- Create: `easyevent-admin-ui/index.html`
- Create: `easyevent-admin-ui/src/main.ts`

**Steps:**
1. Initialize Vue3 + TypeScript + Vite project.
2. Install Element Plus, Axios, Vue Router, ECharts.

### Task 2.2: Event List Page
**Files:**
- Create: `easyevent-admin-ui/src/views/EventList.vue`
- Create: `easyevent-admin-ui/src/api/event.ts`

**Steps:**
1. Implement Table with columns: ID, Class, State, Time, Error Count.
2. Implement Filter Form.
3. Implement Pagination.

### Task 2.3: Dashboard Page
**Files:**
- Create: `easyevent-admin-ui/src/views/Dashboard.vue`
- Create: `easyevent-admin-ui/src/components/Charts/*.vue`

**Steps:**
1. Implement Stats Cards (Total, Success, Failed).
2. Implement Charts using ECharts.

### Task 2.4: Build & Integration
**Files:**
- Modify: `easyevent-admin/pom.xml` (Add frontend maven plugin or manual copy)
- Modify: `easyevent-admin-ui/vite.config.ts` (Build output to backend resources)

**Steps:**
1. Configure Vite to build to `../easyevent-admin/src/main/resources/static`.
2. Ensure Spring Boot serves static files correctly.
