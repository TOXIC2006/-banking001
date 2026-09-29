# SecureBank Modular Monolith

A full-stack digital banking application with a Spring Boot 3 / Java 21 modular monolith and a responsive React + Vite client. It preserves the original service boundaries without service discovery, an API gateway, network clients, or distributed-transaction claims.

## Architecture

```text
React SPA (responsive dashboard, transfers, payments, KYC, statements)
  |
HTTP / relative `/api` requests
  |
  +-- Central Spring Security (JWT + CORS + rate limiting + authorization)
  |
  +-- account -------- onboarding, digital KYC, profiles, CSV e-statements
  +-- transaction ---- MySQL ACID ledger, locked balances, intra-bank transfers
  +-- payment -------- MongoDB payment orchestration, IMPS/NEFT/utility/gateway adapters
  +-- notification --- MongoDB asynchronous OTPs, alerts, receipts, reminders
  +-- fraudhelp ------- MySQL anomaly rules, blocking, failed-transfer tickets
```

Modules communicate through direct Java service calls and narrow internal ports. There are **no** Eureka, Spring Cloud Gateway, or OpenFeign dependencies.

### Persistence

| Store | Modules | Purpose |
|---|---|---|
| MySQL / InnoDB | Account, Transaction, Fraud & Help | Customer records, pessimistically locked balances, immutable double-entry-style ledger records, fraud cases, tickets |
| MongoDB | Payment, Notification | Payment workflow state, notification delivery records, bill reminders |

An intra-bank movement, both ledger entries, and both balance updates share one MySQL `@Transactional` unit. Account rows are locked in ID order to avoid deadlocks. External payments use a saga: commit the MySQL debit, invoke the provider adapter, and post an immutable credit reversal if settlement fails. MySQL and MongoDB are intentionally not presented as one distributed ACID transaction.

## Prerequisites

- JDK 21
- Maven 3.9+ (or `./mvnw`)
- Node.js 20+ and npm 10+ for frontend development (the Maven package build installs its own pinned Node runtime)
- Docker with Compose for local databases

## Configuration and startup

Real credentials are not committed. Copy the template and set your MySQL password locally:

```bash
cp .env.example .env
# Edit .env and set DB_PASSWORD, JWT_SECRET, and optional admin credentials.
docker compose --env-file .env up -d

set -a; source .env; set +a
./mvnw spring-boot:run
```

For frontend development, start Vite in a second terminal. It proxies relative `/api` and `/actuator` requests to Spring Boot on port 8080:

```bash
cd frontend
npm ci
npm run dev
```

Open `http://localhost:5173`. The production frontend uses the supplied palette (`#082F49`, `#0E7490`, `#22C55E`, `#BAE6FD`, `#F0FDFF`) and is built automatically into the Spring Boot JAR by `./mvnw clean package`.

The supplied MySQL user is supported through `DB_USERNAME=root`; put the supplied password in the ignored `.env` file as `DB_PASSWORD`. Do not add it to `application.yml`.

Useful environment variables:

| Variable | Default |
|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/banking?...` |
| `DB_USERNAME` | `root` |
| `DB_PASSWORD` | empty (must be configured) |
| `MONGODB_URI` | `mongodb://localhost:27017/banking` |
| `JWT_SECRET` | development-only value; replace in every deployment |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | empty; when both are set, creates the initial admin |

Flyway creates the MySQL schema. Spring Data creates the declared MongoDB indexes.

## Typical API flow

All protected requests use `Authorization: Bearer <token>`. Every money-moving POST also requires a unique `Idempotency-Key` header.

1. `POST /api/accounts/onboard` — create a customer.
2. `POST /api/auth/login` — receive a JWT.
3. `POST /api/accounts/me/kyc` — submit KYC (the document number is SHA-256 hashed before storage).
4. An admin calls `PUT /api/admin/accounts/{id}/kyc` and `POST /api/admin/accounts/{id}/fund`.
5. `POST /api/transactions/transfers` — perform an ACID intra-bank transfer.
6. `POST /api/payments` — initiate IMPS, NEFT, utility, or gateway settlement.

Example onboarding:

```bash
curl -X POST http://localhost:8080/api/accounts/onboard \
  -H 'Content-Type: application/json' \
  -d '{
    "fullName":"Asha Singh",
    "email":"asha@example.com",
    "phone":"+919876543210",
    "password":"a-strong-password",
    "currency":"INR"
  }'
```

Example login and transfer:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"asha@example.com","password":"a-strong-password"}' | jq -r .accessToken)

curl -X POST http://localhost:8080/api/transactions/transfers \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: transfer-001' \
  -d '{"destinationAccountNumber":"100000000002","amount":500.00,"description":"Rent"}'
```

### Main endpoints

| Module | Endpoints |
|---|---|
| Account | `/api/accounts/onboard`, `/api/accounts/me`, `/api/accounts/me/kyc`, `/api/accounts/me/statements` |
| Transaction | `/api/transactions/transfers`, `/api/admin/accounts/{id}/fund` |
| Payment | `/api/payments`, `/api/payments/{reference}` |
| Notification | `/api/notifications`, `/api/notifications/otp`, `/api/notifications/otp/verify`, `/api/notifications/reminders` |
| Fraud & Help | `/api/help/tickets`, `/api/admin/fraud/cases` |

The included sandbox gateway settles normally and deliberately fails beneficiaries beginning with `FAIL` so reversal and support-ticket behavior can be exercised. `SandboxMessageSender` does not send or log sensitive OTPs; replace the `MessageSender` and `ExternalPaymentGateway` adapter beans with production provider implementations.

## Tests and package

```bash
# React production bundle
cd frontend && npm ci && npm run build && cd ..

# Backend, architecture tests, and full production package
./mvnw test
./mvnw clean package
```

Architecture tests prevent accidental dependencies on Spring Cloud, Eureka/Netflix discovery, or OpenFeign, and ensure Mongo-backed modules do not use JPA.
