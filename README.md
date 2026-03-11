# Backend

This folder contains the backend stack for the recruitment platform.

## Local runtime services
- `recruitment-service` — core Spring Boot Kotlin application
- `api-gateway` — Spring Cloud Gateway entry point with JWT validation and throttling
- `discovery-service` — Eureka service registry
- `postgres` — primary relational database
- `redis` — refresh-token store and dashboard cache
- `kafka` — asynchronous event propagation for application lifecycle events

## What changed in this regenerated build
- local development no longer uses Nginx
- local development runs a single gateway instance on `http://localhost:8080`
- local development removes Kibana / Logstash / Elasticsearch from the default compose
- Kafka is enabled by default locally, but **no API path waits for Kafka before returning**
- application submission and review events are published **after DB commit** on a background executor
- if Kafka is slow, unavailable, or topic creation is delayed, the API still succeeds as long as the database write succeeded

## Tools, technologies, and where they are used

### Core backend
- **Spring Boot + Kotlin** — main service code in `recruitment-service`
- **Spring Security** — API authn/authz in `recruitment-service` and `api-gateway`
- **JWT** — stateless auth tokens in `recruitment-service/src/main/kotlin/.../utilities/security`
- **JPA / Hibernate** — entity persistence in `recruitment-service/src/main/kotlin/.../models/entities`
- **PostgreSQL** — primary data store
- **Flyway** — schema migration in `recruitment-service/src/main/resources/db/migration`
- **Redis** — refresh token storage and dashboard cache
- **Eureka** — service discovery
- **Kafka** — asynchronous application lifecycle event publishing after commit
- **Swagger / OpenAPI** — API docs
- **Mockito + JUnit + JaCoCo** — backend testing and coverage scaffolding
- **Docker** — service containerization
- **Nginx / ELK** — kept in `docker-compose.production.yml` and `infrastructure/` for production-style deployment, not used by default locally

### Security and performance techniques
- **RBAC** — `APPLICANT`, `HR`, `SUPER_ADMIN`
- **Stateless authentication** — JWT access + refresh tokens
- **Rate limiting** — gateway request limiter backed by Redis
- **Sensitive-field encryption at rest** — applicant profile fields via AES-GCM in `FieldEncryptionService`
- **Searchable fingerprinting** — HMAC-based fingerprint for national ID lookups
- **Input validation** — Jakarta Bean Validation
- **Audit logging** — auth, admin, review, and document actions
- **Top-N query optimization** — latest-10 HR query in repository layer
- **Dashboard caching** — Redis-backed cache annotations
- **Fast response path** — request thread writes DB and returns; Kafka is best-effort event propagation only

## Local setup

### Prerequisites
- Docker
- Docker Compose

### Start backend
```bash
cp .env.example .env
docker compose up --build
```

### Local endpoints
- API gateway: `http://localhost:8080`
- API health: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Eureka: `http://localhost:8761`
- Recruitment service direct: `http://localhost:8081`

## Production-style optional services
A production-style overlay is included but not enabled by default locally.

```bash
docker compose -f docker-compose.yml -f docker-compose.production.yml up --build
```

That overlay adds:
- second gateway replica
- Nginx reverse proxy / load balancer
- Elasticsearch
- Logstash
- Kibana

## Seeded accounts
- `admin / Password@123`
- `hr01 / Password@123`
- `applicant01 / Password@123`

## Notes
- `NID` and `NESA` are simulated inside `recruitment-service`.
- Uploaded CVs are stored on the Docker volume mounted at `/data/uploads`.
- TOTP / authenticator-based login is intentionally disabled in this build and should be reintroduced later as a future enhancement.
- The code is optimized so the API path does not block on Kafka, but sub-100 ms response time still depends on machine resources, Docker performance, and cold-start effects.
