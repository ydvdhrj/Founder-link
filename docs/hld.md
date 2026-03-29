# FounderLink — High-Level Design (HLD)

FounderLink is a Spring Boot microservices platform. Clients call the **API Gateway** (`8080`); the gateway discovers routes via **Eureka** and proxies to domain services. **Spring Cloud Config Server** (`8888`) serves centralized YAML from **Git**. Services use **PostgreSQL** (per-domain databases), **MongoDB** for messaging, **RabbitMQ** for events (consumed notably by **notification-service**), and **Zipkin** for distributed tracing.

---

## System context

Paste the block below into [Excalidraw](https://excalidraw.com) via **More tools → Mermaid to Excalidraw** (or your editor’s Mermaid import).

```mermaid
flowchart TB
  subgraph Clients["Clients"]
    WEB["Browser / API clients"]
  end

  subgraph Edge["Edge"]
    GW["API Gateway\nSpring Cloud Gateway :8080"]
  end

  subgraph Platform["Platform services"]
    EUREKA["Eureka Server\n:8761"]
    CONFIG["Config Server\n:8888"]
    GIT[("Git config repo\nfounderlink-config-repo")]
  end

  subgraph Domain["Microservices"]
    AUTH["auth-service\n:8081"]
    USER["user-service\n:8082"]
    STARTUP["startup-service\n:8083"]
    INV["investment-service\n:8084"]
    TEAM["team-service\n:8085"]
    MSG["messaging-service\n:8086"]
    NOTIF["notification-service\n:8087"]
  end

  subgraph Data["Data and messaging"]
    PG[("PostgreSQL\nauth, users, startups,\ninvestments, teams")]
    MONGO[("MongoDB\nmessaging")]
    RMQ["RabbitMQ\n:5672 / mgmt :15672"]
  end

  subgraph Observability["Observability"]
    ZIP["Zipkin\n:9411"]
  end

  WEB --> GW
  GW --> EUREKA
  GW --> AUTH
  GW --> USER
  GW --> STARTUP
  GW --> INV
  GW --> TEAM
  GW --> MSG
  GW --> NOTIF

  CONFIG --> GIT
  AUTH --> CONFIG
  USER --> CONFIG
  STARTUP --> CONFIG
  INV --> CONFIG
  TEAM --> CONFIG
  MSG --> CONFIG
  NOTIF --> CONFIG
  GW --> CONFIG

  AUTH --> EUREKA
  USER --> EUREKA
  STARTUP --> EUREKA
  INV --> EUREKA
  TEAM --> EUREKA
  MSG --> EUREKA
  NOTIF --> EUREKA
  GW --> EUREKA

  AUTH --> PG
  USER --> PG
  STARTUP --> PG
  INV --> PG
  TEAM --> PG
  MSG --> MONGO

  AUTH --> RMQ
  STARTUP --> RMQ
  INV --> RMQ
  TEAM --> RMQ
  MSG --> RMQ
  RMQ --> NOTIF

  AUTH --> ZIP
  USER --> ZIP
  STARTUP --> ZIP
  INV --> ZIP
  TEAM --> ZIP
  MSG --> ZIP
  NOTIF --> ZIP
  GW --> ZIP
  CONFIG --> ZIP
  EUREKA --> ZIP
```

---

## Communication styles

```mermaid
flowchart LR
  subgraph Sync["Synchronous"]
    C[Client] -->|HTTPS REST| G[API Gateway]
    G -->|lb:// via Eureka| S[Microservices]
    A[Service A] -->|OpenFeign HTTP| B[Service B]
  end

  subgraph Async["Asynchronous"]
    P[Publisher service] -->|AMQP publish| X[Topic exchange]
    X --> Q[Queues]
    Q --> N["notification-service\n@RabbitListener"]
  end

  subgraph Cfg["Configuration"]
    APP[Each service] -->|spring.config.import| CS[Config Server]
    CS --> GIT2[Git backend]
  end
```

---

## Local URLs (Docker Compose defaults)

| Component        | URL |
|-----------------|-----|
| API Gateway     | http://localhost:8080 |
| Eureka          | http://localhost:8761 |
| Config Server   | http://localhost:8888 |
| Swagger (GW)    | http://localhost:8080/swagger-ui.html |
| Zipkin          | http://localhost:9411 |
| RabbitMQ UI     | http://localhost:15672 |
