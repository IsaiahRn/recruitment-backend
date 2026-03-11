# Overview

This backend powers the recruitment platform for three user roles:

- Applicant
- HR
- Super Admin

It is built as a Spring Boot + Kotlin service with PostgreSQL as the primary database, Redis for caching/session-related concerns, and optional Kafka for asynchronous application lifecycle events.
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

## Local Run Guide

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
- Redis
- Kafka
- recruitment-service

---

## 4. Check health

Open:

```text
https://rate-limiter-backend-6yj5.onrender.com/actuator/health
```

If you expose the backend through another local port, use that instead.

---

## 5. Swagger / API docs

If Swagger is enabled:

```text
https://rate-limiter-backend-6yj5.onrender.com/swagger-ui/swagger-ui/index.html
```

---

## 6. Running without Docker (optional)

```bash
cd backend/recruitment-service
./gradlew bootRun
```

Make sure PostgreSQL and Redis are already running.

---

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
