# LedgerForge

**LedgerForge** is a Java 21 and Spring Boot 4 backend project being developed incrementally toward a financial ledger system.

The current implementation establishes the backend foundation: REST APIs, PostgreSQL persistence, schema migrations, identity management, JWT authentication, role-based authorization, transaction behavior, and automated testing.

Development is organized into small, verifiable phases, with an emphasis on clear architectural boundaries, security, data integrity, and testable behavior.

**Project status:** The persistence and identity/security foundation is implemented. Financial account management, cards, ledger entries, and money transfers are planned for future phases and are **not yet implemented**.

## Implemented Features

### Identity and Security
- User registration with BCrypt password hashing
- Credential-based login and signed JWT access tokens
- Stateless Bearer-token authentication through Spring Security
- Persistent `USER` and `ADMIN` roles
- Role-based access control for protected endpoints
- Current-user profile retrieval using the authenticated principal
- Explicit `401 Unauthorized` and `403 Forbidden` handling
- Security integration tests covering valid, invalid, tampered, and expired tokens

### Persistence and Data Integrity
- PostgreSQL persistence with Spring Data JPA and Hibernate
- Version-controlled database migrations using Flyway
- Database constraints for user identity and role integrity
- Hibernate schema validation rather than automatic schema generation
- Integration tests for repository persistence and transaction rollback
- Docker Compose configuration for local PostgreSQL

### Backend Architecture
- Separation of controller, service, and repository responsibilities
- Request and response DTOs
- Request validation and structured exception handling
- Constructor-based dependency injection
- Unit, web-layer, and integration testing

## Technology Stack

| Area | Technologies |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 4.1 |
| REST API | Spring Web, Jakarta Validation |
| Security | Spring Security, JWT (JJWT), BCrypt |
| Persistence | Spring Data JPA, Hibernate |
| Database | PostgreSQL |
| Schema management | Flyway |
| Build system | Gradle |
| Local infrastructure | Docker Compose |
| Testing | JUnit 5, Mockito, MockMvc, PostgreSQL integration tests |

## Architecture

LedgerForge follows a layered backend architecture in which HTTP handling, business logic, persistence, and security have distinct responsibilities.

```text
                 HTTP Request
                      |
                      v
           Spring Security Filter Chain
           JWT Validation / Authorization
                      |
                      v
                 Controller
                      |
                      v
                   Service
                      |
                      v
                  Repository
               Spring Data JPA
                      |
                      v
                  PostgreSQL
```

Controllers handle HTTP requests and responses. Services own application behavior and coordinate persistence operations. Repositories provide database access through Spring Data JPA.

Spring Security processes authentication before protected controller execution, establishing the authenticated principal within the `SecurityContext`.

### Current-User Identity Flow

For a request such as `GET /api/profile/me`:

1. The client sends an access token in the `Authorization: Bearer <token>` header.
2. `JwtAuthenticationFilter` validates the JWT signature and expiration.
3. The user UUID is extracted from the token subject.
4. Spring Security establishes an authenticated principal and its authorities.
5. The controller obtains the authenticated user's identity.
6. `ProfileService` retrieves the corresponding user through `UserRepository`.
7. The API returns the current user's profile information.

**The client does not provide a user ID as proof of identity.** The authenticated identity is established through the validated access token.

## API Overview

| Method | Endpoint | Access | Purpose |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | Public | Register a new user |
| `POST` | `/api/auth/login` | Public | Authenticate and issue a JWT |
| `GET` | `/api/profile/me` | Authenticated | Retrieve the current user's profile |
| `GET` | `/api/admin/test` | ADMIN | Verify role-restricted access |
| `GET` | `/actuator/health` | Authenticated | Check application health |

The API also contains basic system endpoints introduced during the backend foundation phase.

### Authentication and Authorization

Newly registered users receive the default `USER` role.

JWT access tokens contain:
- `sub` — authenticated user UUID
- `role` — role assigned when the token is issued
- `iat` — issued-at timestamp
- `exp` — expiration timestamp

A successful login produces a signed access token that can be used for subsequent authenticated requests.

Protected routes require a valid token. Administrative routes additionally require the `ADMIN` authority.

Expected security behavior includes:

| Scenario | Expected response |
| --- | --- |
| Missing token on a protected endpoint | `401 Unauthorized` |
| Tampered or expired JWT | `401 Unauthorized` |
| Valid USER token on an authorized endpoint | `200 OK` |
| USER accessing an ADMIN-only endpoint | `403 Forbidden` |
| ADMIN accessing an ADMIN-only endpoint | `200 OK` |

**JWT role staleness:** Because the role is included when a token is issued, changing a user's role in the database does not automatically update an existing token. The updated role is reflected when a new token is issued.

## Persistence and Transaction Design

PostgreSQL is the primary persistent data store.

Database schema changes are managed with Flyway migrations:

- `V1__create_users_table.sql` — initial user persistence schema
- `V2__add_role_to_users.sql` — persistent role support

Hibernate is configured with:

```properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false
```

This keeps schema evolution under Flyway's control and prevents Hibernate from silently modifying database tables during application startup.

### Transaction Rollback Verification

A dedicated integration test examines the distinction between **flush**, **commit**, and **rollback**.

The test:

1. Starts a transactional persistence operation.
2. Persists an entity through JPA.
3. Explicitly flushes changes so SQL is executed.
4. Forces a runtime exception before the transaction commits.
5. Verifies from outside the failed transaction that the inserted row was not committed.

This demonstrates an important persistence property:

**SQL execution does not guarantee durable persistence.** Changes made inside a transaction may still be rolled back before commit.

The current implementation uses this experiment to establish transaction correctness before introducing future multi-step financial operations.

## Automated Testing

LedgerForge uses multiple testing boundaries to verify behavior at the appropriate level.

### Unit and Service Tests
- Application service behavior
- Registration and login scenarios
- Password encoding and credential verification
- Expected failures and dependency interactions using Mockito

### Web-Layer Tests
- HTTP request handling
- Request validation
- Response behavior
- Structured error responses

### Security Integration Tests
- Missing authentication
- Valid JWT authentication
- USER and ADMIN authorization
- Tampered JWT rejection
- Expired JWT rejection
- Correct `401` and `403` responses

### Persistence Integration Tests
- User repository operations against PostgreSQL
- Persistent role handling
- Password hashing verification after registration
- Transaction rollback after an explicit flush

The password persistence test verifies that a registered user's stored password is not the raw password and that the stored BCrypt hash can be validated through `PasswordEncoder.matches(...)`.

Run the test suite:

```bash
./gradlew test
```

On Windows:

```powershell
.\gradlew.bat test
```

Tests requiring PostgreSQL use the local database configuration described below.

## Running Locally

### Prerequisites

- Java 21
- Docker with Docker Compose
- Git

The Gradle wrapper is included, so a separate Gradle installation is not required.

### 1. Clone the repository

```bash
git clone https://github.com/CanK0KSAL/LedgerForge.git
cd LedgerForge
```

### 2. Configure environment variables

Copy the provided example environment file.

Linux/macOS:

```bash
cp .env.example .env
```

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

Update `.env` with local configuration:

```dotenv
POSTGRES_DB=ledgerforge
POSTGRES_USER=ledgerforge_app
POSTGRES_PASSWORD=replace-with-a-local-password
POSTGRES_PORT=5433

JWT_SECRET=replace-with-a-base64-encoded-random-secret
```

The `JWT_SECRET` must contain at least **32 cryptographically secure random bytes encoded as Base64**.

Generate a suitable secret on systems with OpenSSL:

```bash
openssl rand -base64 32
```

Alternatively, on Windows PowerShell:

```powershell
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
[Convert]::ToBase64String($bytes)
$rng.Dispose()
```

Copy the generated value into the local `.env` file.

**Never commit your actual `.env` file or JWT signing secret to version control.**

### 3. Start PostgreSQL

```bash
docker compose up -d
```

The Compose configuration starts PostgreSQL using the environment variables defined in `.env`.

The default local database port is `5433`.

### 4. Run the application

Linux/macOS:

```bash
./gradlew bootRun
```

Windows:

```powershell
.\gradlew.bat bootRun
```

The application starts at:

`http://localhost:8080`

Flyway applies pending database migrations during application startup.

### 5. Run the tests

With the database running, open another terminal.

Linux/macOS:

```bash
./gradlew clean test
```

Windows:

```powershell
.\gradlew.bat clean test
```

The test suite runs unit, web-layer, security, and persistence tests.

### 6. Stop the database

```bash
docker compose down
```

The named Docker volume preserves local database data unless explicitly removed.

## Development Roadmap

| Phase | Scope | Status |
| --- | --- | --- |
| Foundation | Spring Boot application, REST endpoints, validation, error handling, testing | Completed |
| Persistence | PostgreSQL, JPA/Hibernate, Flyway, repository integration | Completed |
| Identity and Security | Registration, login, JWT, authentication, RBAC, security testing | Implemented |
| Customer and Account Domain | Financial customers, accounts, ownership rules | Planned |
| Card Domain | Card management and associated business rules | Planned |
| Ledger | Financial entries, balances, consistency and auditability | Planned |
| Transfers | Transactional money movement, concurrency and failure handling | Planned |

The roadmap describes intended development direction, not already delivered functionality.

## Engineering Focus

LedgerForge is structured around several backend engineering principles:

- **Explicit boundaries:** HTTP, security, application logic, and persistence responsibilities remain separate.
- **Security by design:** Authentication is established from validated credentials and tokens rather than client-provided identity claims.
- **Database integrity:** Relational constraints, schema migrations, and transaction boundaries are treated as part of application correctness.
- **Failure-oriented testing:** Invalid credentials, unauthorized access, malformed tokens, and failed persistence operations are tested deliberately.
- **Incremental development:** Features are developed and verified in focused steps using Git branches and pull requests.

## Current Limitations

LedgerForge is an actively developed backend project, **not a production banking service**.

The current version does not yet implement financial accounts, cards, ledger posting, money transfers, or production-grade financial infrastructure.

These domains will be introduced in later phases with their own business rules, persistence models, transactional boundaries, and tests.

The existing implementation provides the technical foundation on which those features will be built.
