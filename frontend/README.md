# FounderLink – Angular Frontend

Angular **21** single-page application for the FounderLink microservices platform.
It connects to the Spring Cloud **API Gateway** (default `http://localhost:8080`) and exposes:

- JWT-based login / signup / logout
- Role-aware dashboard (Founder, Investor, Co-founder, Admin)
- Public directory of members + detailed profile view
- Create / edit personal profile
- Discover, create and manage startups
- Submit, list, approve / reject investments
- Invite co-founders / team members
- Direct messaging between members

## 1. Prerequisites

- Node.js **20+**
- Backend running on the gateway port (`8080`) — see the root `docker-compose.yml`.

Start the full backend (Eureka, Config, Gateway, Auth, User, Startup, Investment, Team, Messaging, Notification + Postgres/Mongo/Rabbit/Zipkin):

```bash
cd ..
./run-docker.sh         # docker compose up
# or locally:
./run-all-services.sh
```

## 2. Install & run

```bash
cd frontend
npm install
npm start               # ng serve, opens at http://localhost:4200
```

Change the gateway URL in `src/environments/environment.ts`:

```ts
apiBaseUrl: 'http://localhost:8080'
```

## 3. Build for production

```bash
npm run build
# output: dist/frontend
```

## 4. Architecture

```
src/
├── environments/
│   ├── environment.ts            API base URL + storage key (dev)
│   └── environment.prod.ts       Production variant
└── app/
    ├── app.ts                    Root shell, just <router-outlet>
    ├── app.config.ts             Providers: router, HttpClient, interceptors
    ├── app.routes.ts             Lazy-loaded feature routes + guards
    │
    ├── core/                     Cross-cutting, app-wide
    │   ├── models/               TypeScript interfaces mirroring Java DTOs
    │   ├── services/             HTTP clients (one per microservice)
    │   ├── interceptors/         auth (adds Bearer), error (401 → /login)
    │   └── guards/               authGuard, guestGuard, roleGuard
    │
    ├── layout/
    │   └── shell/                Sidebar + topbar for authenticated pages
    │
    └── features/                 Feature pages (lazy-loaded)
        ├── auth/                 login, signup
        ├── dashboard/            role-aware landing page
        ├── profile/              my-profile, directory, detail, form
        ├── startups/             list, create, detail (+ invest panel)
        ├── investments/          my portfolio
        ├── teams/                invite co-founder
        └── messages/             contacts + chat view
```

### State management
- **Auth** uses Angular Signals (`AuthService.state`, `currentUser`, `roles`).
- Tokens persist in `localStorage` under the key `founderlink.auth`.
- On 401, the error interceptor clears the session and redirects to `/login?expired=1`.

### Backend endpoints consumed

| Feature       | Endpoint (via API Gateway `:8080`)                                   |
|---------------|-----------------------------------------------------------------------|
| Auth          | `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh`, `GET /auth/me` |
| Profiles      | `GET /users`, `GET /users/{id}`, `POST /users`, `PUT /users/{id}`, `GET /users/lookup?email=` |
| Startups      | `GET /startups`, `POST /startups`, `GET /startups/{id}`              |
| Investments   | `POST /investments`, `GET /investments/investor`, `GET /investments/startup/{id}`, `PUT /investments/{id}/status` |
| Teams         | `POST /teams/invite`, `POST /teams/join/{inviteId}`, `GET /teams/startup/{id}` |
| Messaging     | `POST /messages`, `GET /messages/conversation/{otherUserId}`         |

The gateway forwards the JWT, extracts `userId` into `X-User-Id` and enforces role-based routing.

## 5. Role-gated navigation

| Route                | Allowed roles                          |
|----------------------|----------------------------------------|
| `/dashboard`         | any authenticated user                 |
| `/profile`           | any authenticated user                 |
| `/directory`         | any authenticated user                 |
| `/startups`          | any authenticated user                 |
| `/startups/new`      | `ROLE_FOUNDER`, `ROLE_ADMIN`           |
| `/investments`       | any authenticated user (filled for `ROLE_INVESTOR`) |
| `/teams/invite`      | `ROLE_FOUNDER`, `ROLE_ADMIN`           |
| `/messages`          | any authenticated user                 |

## 6. Scripts

| Script          | Description                          |
|-----------------|--------------------------------------|
| `npm start`     | Dev server with HMR                  |
| `npm run build` | Production build to `dist/frontend`  |
| `npm run watch` | Dev build in watch mode              |
| `npm test`      | Unit tests (vitest)                  |

## 7. CORS

The Spring Cloud Gateway must allow `http://localhost:4200` during development.
If requests fail with a CORS error, add to `api-gateway.yml`:

```yaml
spring:
  cloud:
    gateway:
      server:
        webflux:
          globalcors:
            cors-configurations:
              '[/**]':
                allowedOriginPatterns: ['http://localhost:4200']
                allowedMethods: ['GET','POST','PUT','DELETE','OPTIONS']
                allowedHeaders: ['*']
                allowCredentials: true
```
