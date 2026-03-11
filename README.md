# Backend README

## Overview

This backend powers the recruitment platform for three user roles:

- Applicant
- HR
- Super Admin

It is built as a Spring Boot + Kotlin service with PostgreSQL as the primary database, Redis for caching/session-related concerns, and optional Kafka for asynchronous application lifecycle events.

The current design follows one important rule:

- API success depends on **database success**
- API success does **not** depend on Kafka success

That means:
- if the database write succeeds, the API should return success
- Kafka is treated as event propagation, not the main transaction boundary

---

## Features and User Flows

### 1. Authentication
All users must log in before performing protected actions.

Supported roles:
- `APPLICANT`
- `HR`
- `SUPER_ADMIN`

### 2. Applicant Flow
An applicant can:

1. Register an account
2. Log in
3. Enter a National ID
4. Verify ID using the simulated NID API
5. Verify academic records using the simulated NESA API
6. Upload a CV
7. Submit the application
8. Track application status and progress history

### 3. HR Flow
HR users can:

1. Log in
2. View dashboard statistics
3. View the latest 10 applicants
4. See those 10 sorted alphabetically for display
5. Select an application
6. View applicant details and attached CV
7. Move application to:
   - Under Review
   - Approved
   - Rejected (with reason)

### 4. Super Admin Flow
Super Admin users can:

1. Log in
2. View dashboard statistics
3. Create users
4. Edit users
5. Enable/disable users
6. View audit logs

### 5. Simulated Integrations
The platform simulates:

- **NID API**  
  Used to populate applicant identity/profile data after entering a National ID

- **NESA API**  
  Used to populate grade and option attended after entering the same National ID

---

## Architecture

### High-Level Structure

The local development architecture is intentionally simplified for stability:

- `recruitment-service` → main API
- PostgreSQL → primary relational database
- Redis / Valkey → cache / token / app support
- Kafka → optional event propagation
- Frontend talks directly to the backend API
- No Nginx in the local setup
- No API gateway in the deployment-critical local path unless explicitly enabled

### Design Principles

- Modular monolith structure
- Role-based access control (RBAC)
- DTO-first API responses
- Validation at API boundaries
- Sensitive-field encryption at rest
- Kafka is non-blocking and optional
- Local runtime is optimized for simplicity and reduced moving parts

### Event Design
Kafka is used only where asynchronous propagation is useful, for example:

- application lifecycle events
- audit/event streams
- future notifications or analytics

But the backend is designed so:
- slow Kafka must not hang the request
- unavailable Kafka must not fail the request if DB commit succeeded

---

## Tools Used and Why

### Core Backend Stack
- **Kotlin**
  - Main backend language
  - Used for controllers, services, DTOs, repositories, configuration

- **Spring Boot**
  - Main application framework
  - Used for REST APIs, dependency injection, configuration, security, validation

- **Spring Security**
  - Used for authentication and authorization
  - Protects endpoints based on user roles

- **JWT**
  - Used for stateless authentication
  - Access and refresh token model

- **Spring Data JPA / Hibernate**
  - Used for ORM and data persistence
  - Maps Kotlin entities to PostgreSQL tables

- **PostgreSQL**
  - Primary relational database
  - Stores users, applicant profiles, applications, documents metadata, audit data

- **Redis / Valkey**
  - Used for caching and app support
  - Can also support token/session-related concerns

### Messaging
- **Kafka**
  - Used for asynchronous application lifecycle events
  - Not required for API success path

### API and Developer Experience
- **Spring Validation / Jakarta Validation**
  - Used for request validation

- **OpenAPI / Swagger**
  - Used for API documentation

- **Mockito**
  - Used for backend unit testing

### Security and Data Protection
- **Field Encryption**
  - Used for encrypting sensitive applicant fields at rest

- **RBAC**
  - Role-based access control for Super Admin, HR, and Applicant

### Deployment and Runtime
- **Docker**
  - Used for local containerized backend runtime
  - Helps package the backend consistently

- **Docker Compose**
  - Used to run backend dependencies locally
  - Coordinates PostgreSQL, Redis, and optionally Kafka

### Future / Optional Enterprise Components
Depending on environment or future expansion:
- Eureka
- ELK stack
- API gateway
- Blue/green or rolling deployment strategies

---

## Local Run Guide

## Prerequisites

Install:
- Docker Desktop
- Java 21 (optional if running only with Docker)
- Gradle (optional if running only with Docker)

---

## 1. Go to backend directory

```bash
cd backend
```

---

## 2. Configure environment variables

Create a `.env` file if one does not exist.

Example:

```env
POSTGRES_DB=recruitment
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

JWT_SECRET=replace-with-a-long-secret
JWT_ACCESS_TTL_MINUTES=15
JWT_REFRESH_TTL_DAYS=7

DATA_ENCRYPTION_SECRET=replace-with-a-long-encryption-secret
DATA_HASH_SECRET=replace-with-a-long-hash-secret

SPRING_DATA_REDIS_URL=redis://redis:6379

APP_KAFKA_ENABLED=false
```

---

## 3. Start local services

```bash
docker compose up --build
```

This should start:
- PostgreSQL
- Redis / Valkey
- Kafka (if enabled in compose)
- recruitment-service

---

## 4. Check health

Open:

```text
http://localhost:8081/actuator/health
```

If you expose the backend through another local port, use that instead.

---

## 5. Swagger / API docs

If Swagger is enabled:

```text
http://localhost:8081/swagger-ui/index.html
```

---

## 6. Running without Docker (optional)

```bash
cd backend/recruitment-service
./gradlew bootRun
```

Make sure PostgreSQL and Redis are already running.

---

## Important Local Notes

### Kafka
For local testing, Kafka can be disabled:

```env
APP_KAFKA_ENABLED=false
```

### OTP / TOTP
OTP/TOTP was removed from the active flow to simplify testing.  
It should be considered a **future enhancement**, not part of the current test flow.

### NID / NESA
These integrations are simulated internally.  
No real government API credentials are required.

---

## Demo Accounts

### Super Admin
- Username: `admin`
- Password: `Password@123`

### HR
- Username: `hr01`
- Password: `Password@123`

### Applicant
- Username: `applicant01`
- Password: `Password@123`

---

## Production Notes

For production-like deployment:
- use managed Postgres
- use managed Redis / Valkey
- optionally enable Kafka
- store all secrets in environment variables or secret manager
- enable health checks
- use rolling or blue/green deployment strategy
- keep API success independent from Kafka availability

---

## Current Testing Assumptions

This project is optimized for:
- test/demo environment
- smooth local setup
- minimal downtime deployments
- no hard dependency on Kafka for request success
