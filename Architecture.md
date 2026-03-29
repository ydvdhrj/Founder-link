# FounderLink Architecture

## 1) High-Level Design (HLD) and System Overview

### Executive Summary
FounderLink is a Spring Boot `4.0.4` microservices platform that combines synchronous request/response APIs with asynchronous domain events. The system follows an **Event-Driven Microservices** architecture:

- **Synchronous path:** Client -> API Gateway -> target microservice(s) via HTTP/OpenFeign.
- **Asynchronous path:** business services publish domain events to RabbitMQ topic exchange for downstream consumers (notably notifications and future workflow decoupling).
- **Platform controls:** Eureka for discovery, Config Server for centralized configuration, JWT-based RBAC at the edge and service boundaries, Micrometer/Zipkin for distributed tracing.

### Infrastructure Stack (from code and config)
- **Language/runtime:** Java `17`.
- **Framework:** Spring Boot `4.0.4` (all services).
- **Cloud stack:** Spring Cloud BOM `2025.1.1`.
- **Edge/API routing:** Spring Cloud Gateway (WebFlux) in `api-gateway`.
- **Service discovery:** Eureka Server + Eureka clients.
- **Centralized config:** Spring Cloud Config Server + config clients.
- **Databases:**
  - PostgreSQL for transactional domain services (`auth`, `user`, `startup`, `investment`, `team`).
  - MongoDB for high-write chat messaging (`messaging-service`).
- **Messaging/event bus:** RabbitMQ (`spring-boot-starter-amqp`) with topic exchange `founderlink-exchange`.
- **Security:** Spring Security + JJWT (`0.11.5`) for JWT issuance/validation.
- **Observability:** Actuator + Micrometer Tracing (Brave bridge) + Zipkin reporter.
- **API docs:** Springdoc OpenAPI (`springdoc.version=3.0.2` in services using docs UI).

### Boot Sequence (strict operational order)
Boot order is constrained by Config and Discovery wiring:

1. **Infrastructure first**
   - PostgreSQL, MongoDB, RabbitMQ, Zipkin.
2. **`eureka-server`**
   - Discovery backbone; all clients register here.
3. **`config-server`**
   - Serves externalized YAML config from `founderlink-config-repo`.
4. **Backend services**
   - `auth`, `user`, `startup`, `investment`, `team`, `messaging`, `notification`.
5. **`api-gateway` last**
   - Depends on route targets being discoverable.

Why non-negotiable:
- Most services use `spring.config.import=configserver:http://localhost:8888` (hard dependency on config server).
- Services and gateway route to logical service IDs (`lb://...`) and Feign names, requiring Eureka readiness.

### Network Flow (request lifecycle)
Typical flow for authenticated business APIs:

1. **Client -> API Gateway**
   - Request hits gateway route predicate (`/auth/**`, `/users/**`, `/startups/**`, etc.).
2. **Gateway JWT processing**
   - `Bearer` token extracted.
   - Signature/claims validated in gateway (`type=access`, `roles` mapped to authorities).
   - Route-level RBAC enforced in gateway `SecurityWebFilterChain`.
3. **Gateway -> target microservice**
   - Routed via `lb://service-name` using Eureka discovery.
4. **Service internal processing**
   - Optional sync validation call to another service (Feign + circuit breaker).
   - Domain data persistence.
   - Optional RabbitMQ event publish.

Notes from code:
- Gateway performs local JWT validation; no runtime gateway call to `/auth/validate`.
- Several services consume identity via `X-User-Id` header.

---

## 2) Low-Level Design (LLD): Service Registry

### eureka-server
- **Port / DB:** `8761` / None.
- **Core responsibilities:**
  - Hosts service registry.
  - Enables service discovery for all clients.
  - Runs as non-client (`register-with-eureka=false`, `fetch-registry=false`).
- **Key API endpoints:** Eureka native endpoints (no custom REST controllers).
- **Inter-service communication:**
  - **Sync:** None.
  - **Async:** None.

### config-server
- **Port / DB:** `8888` / None (Git-backed config source).
- **Core responsibilities:**
  - Centralized external configuration for all services.
  - Serves per-service YAML from `founderlink-config-repo`.
  - Registers to Eureka as a client.
- **Key API endpoints:** Spring Cloud Config endpoints (no custom REST controllers).
- **Inter-service communication:**
  - **Sync:** Config clients fetch config over HTTP.
  - **Async:** None.

### api-gateway
- **Port / DB:** `8080` / None.
- **Core responsibilities:**
  - Single ingress/API routing to backend services.
  - JWT validation and route-level RBAC enforcement.
  - Swagger aggregation/proxy routing.
- **Key API endpoints/routes exposed:**
  - Route predicates in config: `/auth/**`, `/users/**`, `/startups/**`, `/investments/**`, `/teams/**`, `/messages/**`.
- **Inter-service communication:**
  - **Sync:** Routes to `auth-service`, `user-service`, `startup-service`, `investment-service`, `team-service`, `messaging-service` using `lb://`.
  - **Async:** None.

### auth-service
- **Port / DB:** `8081` / PostgreSQL (`auth` DB).
- **Core responsibilities:**
  - User registration/login.
  - Access and refresh token issuance.
  - Token validation and current user lookup.
- **Key API endpoints:**
  - `POST /auth/register`
  - `POST /auth/login`
  - `POST /auth/refresh`
  - `POST /auth/validate`
  - `GET /auth/me`
- **Inter-service communication:**
  - **Sync:** None outgoing.
  - **Async:** None visible.

### user-service
- **Port / DB:** `8082` / PostgreSQL (`user_service` DB).
- **Core responsibilities:**
  - Profile CRUD and retrieval.
  - Profile directory/listing.
  - Authorization for profile mutation paths.
- **Key API endpoints:**
  - `GET /users`
  - `GET /users/{userId}`
  - `POST /users`
  - `PUT /users/{userId}`
- **Inter-service communication:**
  - **Sync:** None outgoing in current code.
  - **Async:** None visible.

### startup-service
- **Port / DB:** `8083` (default from config repo) / PostgreSQL (`startups_db`).
- **Core responsibilities:**
  - Startup creation and listing.
  - Startup details retrieval with founder enrichment.
  - Publishes startup-created events.
- **Key API endpoints:**
  - `POST /startups`
  - `GET /startups/{id}`
  - `GET /startups`
- **Inter-service communication:**
  - **Sync:** Feign call to `user-service` (`GET /users/{id}`), wrapped with Resilience4j circuit breaker `userService`.
  - **Async:** Publishes `startup.created` to RabbitMQ exchange `founderlink-exchange`.

### investment-service
- **Port / DB:** `8084` / PostgreSQL (`investments_db`).
- **Core responsibilities:**
  - Create investment records.
  - Query investments by startup/investor.
  - Update investment status lifecycle.
- **Key API endpoints:**
  - `POST /investments`
  - `GET /investments/startup/{startupId}`
  - `GET /investments/investor`
  - `PUT /investments/{id}/status`
- **Inter-service communication:**
  - **Sync:** Feign call to `startup-service` (`GET /startups/{id}`), with circuit breaker `startupService`.
  - **Async:** Publishes `investment.created` to `founderlink-exchange`.

### team-service
- **Port / DB:** `8085` / PostgreSQL (`teams_db`).
- **Core responsibilities:**
  - Team invitation flow.
  - Invitation acceptance and membership state tracking.
  - Team listing per startup.
- **Key API endpoints:**
  - `POST /teams/invite`
  - `POST /teams/join/{inviteId}`
  - `GET /teams/startup/{startupId}`
- **Inter-service communication:**
  - **Sync:** Feign call to `startup-service` (`GET /startups/{id}`), with circuit breaker `startupService`.
  - **Async:** Publishes `team.invite` to `founderlink-exchange`.

### messaging-service
- **Port / DB:** `8086` / MongoDB (`founderlink_messages` DB).
- **Core responsibilities:**
  - Send direct messages.
  - Retrieve ordered two-way conversation history.
  - Publish message-sent events.
- **Key API endpoints:**
  - `POST /messages`
  - `GET /messages/conversation/{otherUserId}`
- **Inter-service communication:**
  - **Sync:** Feign call to `user-service` (`GET /users/{id}`), with circuit breaker `userService`.
  - **Async:** Publishes `message.sent` to `founderlink-exchange`.

### notification-service
- **Port / DB:** `8087` / Not explicitly configured in external YAML.
- **Core responsibilities (visible in current workspace):**
  - Service bootstrap and discovery client registration.
  - RabbitMQ and mail stack bootstrap readiness.
  - Placeholder for notification orchestration.
- **Key API endpoints:** No production controllers discovered in main source.
- **Inter-service communication:**
  - **Sync:** None visible.
  - **Async:** No `@RabbitListener` consumers found in current source.

---

## 3) Data Architecture and Event-Driven Flow

### Data Stores
- **PostgreSQL (transactional domain services):**
  - Used where strong consistency and relational integrity are required:
    - auth/user identities and roles,
    - startup and investment lifecycle,
    - team membership and invitation state.
- **MongoDB (messaging):**
  - Used for flexible, high-volume message documents and conversation retrieval patterns.
  - Message schema naturally fits document model (`Message` with sender/receiver/content/timestamp/read).

### RabbitMQ Event Topology (from code)
All publishing services use topic exchange: `founderlink-exchange`.

- **Startup domain**
  - Publisher: `startup-service`
  - Routing key: `startup.created`
  - Payload type: `StartupCreatedEvent` (`eventType=STARTUP_CREATED`)
- **Investment domain**
  - Publisher: `investment-service`
  - Routing key: `investment.created`
  - Payload type: `InvestmentCreatedEvent` (`eventType=INVESTMENT_CREATED`)
- **Team domain**
  - Publisher: `team-service`
  - Routing key: `team.invite`
  - Payload type: `TeamInviteSentEvent` (`eventType=TEAM_INVITE_SENT`)
- **Messaging domain**
  - Publisher: `messaging-service`
  - Routing key: `message.sent`
  - Payload type: `NewMessageEvent` (`eventType=NEW_MESSAGE`)

Current-state caveat:
- Exchange + publisher patterns are implemented.
- Explicit queue bindings and `@RabbitListener` consumers are not visible in scanned code, including `notification-service`.

---

## 4) Security and Observability

### Role-Based Access Control (RBAC)
- **Auth token composition (`auth-service`):**
  - Access token includes:
    - `type=access`,
    - `userId`,
    - `roles` claim (e.g., `ROLE_FOUNDER`, `ROLE_INVESTOR`, `ROLE_ADMIN`).
  - Refresh token includes `type=refresh`.
- **Gateway enforcement (`api-gateway`):**
  - Bearer token parsed from header.
  - JWT signature and claims verified locally.
  - Authorities derived from `roles` claim.
  - Route-level rules in gateway security config:
    - `/auth/**` public,
    - `/users/**` authenticated,
    - selected role-based route restrictions (`hasRole(...)`).

Architectural note:
- Gateway currently validates JWT locally rather than calling auth introspection endpoint.
- Ensure route matchers are kept aligned with actual controller paths to avoid authorization drift.

### Tracing and Logging
- **Dependencies present in all services:**
  - `spring-boot-starter-actuator`
  - `micrometer-tracing-bridge-brave`
  - `zipkin-reporter-brave`
- **Runtime tracing config (externalized YAML):**
  - `management.tracing.enabled: true`
  - `management.tracing.sampling.probability: 1.0`
  - `management.tracing.export.zipkin.endpoint: http://localhost:9411/api/v2/spans`
- **Log correlation pattern:**
  - Trace/span fields embedded in log pattern with `%X{traceId}` and `%X{spanId}` for cross-service request correlation.

---

## Known Gaps and Non-Discoverable Areas
- No explicit RabbitMQ consumers (`@RabbitListener`) or queue/binding definitions were found in scanned business services.
- `notification-service` currently lacks visible business endpoints and event consumption logic in main source.
- No explicit code was found in gateway showing `X-User-Id` injection, although downstream services depend on that header.
- Gateway route security includes `POST /startups/create`, while startup controller exposes `POST /startups`; verify intended protected path mapping.
