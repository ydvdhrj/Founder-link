# FounderLink

FounderLink is a microservices platform for founders, investors, and team members: profiles, startups, investments, team invites, and direct messaging. The backend is **Spring Boot 4** and **Spring Cloud 2025**; the web UI is an **Angular 21** SPA that talks to a single **Spring Cloud Gateway** entry point.

## Documentation

- **[Architecture.md](Architecture.md)** — system overview, HLD/LLD diagrams, services, data stores, events, and security.
- **[frontend/README.md](frontend/README.md)** — Angular app structure, scripts, and API usage from the browser.

## Tech stack (summary)

| Area | Technology |
|------|------------|
| API edge | Spring Cloud Gateway (WebFlux), JWT + RBAC, Eureka discovery |
| Services | Auth, User, Startup, Investment, Team, Messaging, Notification |
| Config | Spring Cloud Config Server (Git-backed; see `config-server` and `founderlink-config-repo/`) |
| Data | PostgreSQL (per-domain DBs), MongoDB (messaging) |
| Events | RabbitMQ topic exchange `founderlink-exchange` |
| Observability | Spring Boot Actuator, Zipkin |
| Frontend | Angular 21, TypeScript, Vitest |

## Prerequisites

- **Docker** and **Docker Compose** (recommended for the full stack), or
- **JDK 17+**, **Maven 3.8+**, and infrastructure matching `docker-compose.yml` (Postgres, MongoDB, RabbitMQ, etc.) if you run services locally with `./run-all-services.sh`.
- For the UI: **Node.js 20+** and `npm` (see `frontend/README.md`).

## Quick start (Docker)

From the repository root:

```bash
./run-docker.sh
```

This builds images and runs Postgres, MongoDB, RabbitMQ, Zipkin, Eureka, Config Server, all microservices, and the API gateway. Useful URLs after startup:

| What | URL |
|------|-----|
| API Gateway (Swagger UI) | http://localhost:8080/swagger-ui.html |
| Eureka dashboard | http://localhost:8761 |
| Config Server health | http://localhost:8888/actuator/health |
| Zipkin | http://localhost:9411 |
| RabbitMQ management | http://localhost:15672 (guest / guest) |

Postgres is exposed on host port **5433** (container `5432`). The stack uses the Git config repository URL defined in `docker-compose.yml` / `config-server` (override with `SPRING_CLOUD_CONFIG_SERVER_GIT_URI` if you fork config).

## Quick start (local JVM processes)

If you prefer JARs via Maven on the host (and your own or Docker-only infra), use:

```bash
./run-all-services.sh
```

The script expects Java 17+ and starts services in order; logs go under `logs/`. You still need Eureka, Config Server, and databases reachable as in your `application` / config-repo settings.

## Frontend (optional)

With the gateway on `http://localhost:8080`:

```bash
cd frontend
npm install
npm start
```

The dev server defaults to **http://localhost:4200**. Set `apiBaseUrl` in `frontend/src/environments/environment.ts` if your gateway host or port differs.

## Repository layout

```
FounderLink/
├── api-gateway/            # Spring Cloud Gateway
├── auth-service/
├── user-service/
├── startup-service/
├── investment-service/
├── team-service/
├── messaging-service/
├── notification-service/
├── eureka-server/
├── config-server/
├── founderlink-config-repo/   # Local copy of Config Server YAML (source of truth is Git for runtime)
├── frontend/                  # Angular SPA
├── docker-compose.yml
├── run-docker.sh
├── run-all-services.sh
├── Architecture.md
└── README.md
```

## Configuration

- Services import **Spring Cloud Config** (`configserver:…` in `application.yml`). For production, set `JWT_SECRET` and database URLs via environment or your config repository.
- Gateway routes, ports, and shared settings are defined in the **config repository** (see `founderlink-config-repo/api-gateway.yml` in this tree for reference).
