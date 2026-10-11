# ZeroWaste Backend

A modular Spring Boot REST API for **ZeroWaste**, a peer-to-peer reuse platform where people list items they no longer need, make offers, and arrange giveaways. The backend also supports reviews, community blog posts, moderation, OAuth sign-in, and real-time notifications.

## Table of contents

- [Core features](#core-features)
- [Technology stack](#technology-stack)
- [Architecture](#architecture)
- [Image upload and storage](#image-upload-and-storage)
- [Redis: caching and rate limiting](#redis-caching-and-rate-limiting)
- [Authentication and security](#authentication-and-security)
- [Requirements](#requirements)
- [Configuration](#configuration)
- [Run locally](#run-locally)
- [Docker](#docker)
- [API documentation](#api-documentation)
- [Database migrations](#database-migrations)
- [Testing](#testing)
- [Production notes](#production-notes)

## Core features

- **Item marketplace** — create and manage item listings, browse available items, and organize listings by category.
- **Offers** — submit offers for items and handle acceptance/rejection outcomes.
- **Reviews** — publish reviews associated with completed giveaways.
- **Community blog** — create and manage blog content.
- **Authentication** — JWT-based API authentication, refresh-token flow, and OAuth sign-in with Google and GitHub.
- **Authorization and moderation** — role-aware operations, user moderation, and visibility rules.
- **Real-time notifications** — WebSocket communication using STOMP, alongside REST operations.
- **Image uploads** — S3-compatible object storage with presigned upload URLs, so image bytes can travel directly from the browser to storage.
- **Caching** — Redis-backed Spring Cache.
- **Rate limiting** — Bucket4j with Lettuce/Redis integration for shared rate-limit state.
- **Database migrations** — Flyway-managed PostgreSQL schema.
- **API documentation** — OpenAPI specification and Swagger UI.

## Technology stack

| Technology | Role |
|---|---|
| Java 25 | Runtime and language |
| Spring Boot 4.0.7 | Application framework |
| Spring Security | API security |
| JJWT | JWT creation and validation |
| Spring Data JPA / Hibernate | Persistence |
| PostgreSQL | Main relational database |
| Flyway | Versioned database migrations |
| Redis | Cache backend and rate-limit storage |
| Spring Cache | Cache abstraction |
| Bucket4j + Lettuce | Rate limiting backed by Redis |
| Spring Modulith 2.1.1 | Modular monolith structure and application events |
| AWS SDK for Java v2 (S3) | S3-compatible object storage and presigned requests |
| Spring WebSocket + STOMP | Real-time updates |
| Springdoc OpenAPI | API documentation |
| H2 | Test database support |
| Testcontainers | PostgreSQL integration-test infrastructure |
| Maven Wrapper | Reproducible build commands |
| Docker | Container packaging |

Versions and dependencies are based on the repository's Maven build configuration.

## Architecture

The backend is implemented as a **modular monolith**. It is deployed as one Spring Boot application, while code is divided into business-focused modules to keep responsibilities separated.

At a high level:

```text
Web frontend
    |
    | HTTPS / REST
    v
Spring Boot API
    |
    +-- Spring Security / JWT / OAuth
    +-- Business modules
    |     +-- Users and authentication
    |     +-- Items and categories
    |     +-- Offers and reviews
    |     +-- Blog and moderation
    |     +-- Images and notifications
    |
    +-- PostgreSQL  (persistent application data)
    +-- Redis       (cache + distributed rate-limit state)
    +-- S3 API      (image objects)
    |
    +-- WebSocket / STOMP notifications
```

Spring Modulith dependencies and event support help modules communicate without requiring every feature to depend directly on the internals of another module. PostgreSQL is the source of truth for application records; Redis is used for performance and request-control concerns rather than as the primary database.

## Image upload and storage

Images are stored in an **S3-compatible object store**, rather than being served from a directory inside the backend container. The application uses the AWS SDK for Java v2 S3 client, allowing the configured endpoint to point to AWS S3 or a compatible service used for local development.

### Presigned upload flow

The intended upload flow separates authorization/metadata work from the actual file transfer:

1. The frontend asks the backend for presigned upload URL(s).
2. The backend validates the request and creates pending-upload metadata.
3. The backend returns short-lived presigned URL(s) and the required HTTP method/headers.
4. The browser uploads the image directly to object storage using the presigned `PUT` URL.
5. The frontend submits the uploaded image identifiers when creating or updating an item.
6. The backend verifies the pending upload metadata and associates confirmed images with the relevant domain record.

This approach avoids sending the image body through the Spring Boot server and reduces application-server bandwidth and request-processing overhead.

### Storage configuration

The following properties are configured under `app.aws` in `application.yml`:

| Environment variable | Default in configuration | Purpose |
|---|---|---|
| `AWS_ENDPOINT` | `http://localhost:4566` | S3 API endpoint used by the backend |
| `AWS_PUBLIC_ENDPOINT` | `http://localhost:4566` | Endpoint embedded in URLs intended for client access |
| `AWS_ACCESS_KEY_ID` | `test` | Access key for the configured object store |
| `AWS_SECRET_ACCESS_KEY` | `test` | Secret key for the configured object store |
| `AWS_DEFAULT_REGION` | `us-east-1` | S3 signing region |
| `AWS_S3_BUCKET` | `zerowaste-images` | Bucket name |
| `AWS_S3_PATH_STYLE` | `true` | Enables path-style addressing when required by the S3-compatible service |

The defaults are suitable only for a local emulator. Set endpoint, credentials, region, bucket, and URL behavior for your actual environment. When the backend runs in Docker, `localhost` means the backend container itself; use the appropriate internal service address for the S3 endpoint and a browser-reachable public endpoint for URLs returned to the frontend.

### Browser and object-store CORS

Direct browser uploads require the object store's CORS policy to allow the frontend origin, `PUT`, and the request headers used by the presigned request. The browser must send the content type and any signed headers exactly as expected by the URL.

Presigned URLs are bearer credentials: anyone who obtains one may use it until it expires, within the permissions encoded in the signature. Keep expiry short, do not log full URLs, and never expose long-lived object-store credentials to the frontend.

## Redis: caching and rate limiting

Redis has two distinct responsibilities in this application.

### 1. Application caching

Spring Cache is configured to use Redis as its cache provider. This lets cacheable data be reused between requests without repeatedly running the corresponding database/service operation. The configuration disables caching of null values.

A separate cache manager can be configured for caches that need different serialization or expiry policies. Cache TTLs and invalidation should match the data's update behavior: cached values must not become the authoritative source for mutable application state.

### 2. Rate limiting

The build includes Bucket4j's core, Lettuce integration, and Redis support. This allows rate-limit buckets to use Redis-backed state instead of relying only on an in-memory counter in a single JVM.

Redis-backed limits are useful when the application is restarted or runs in more than one instance, because requests can share rate-limit state. When a limit is exceeded, the API can reject further requests until the applicable bucket permits them again.

### Redis connection settings

The following settings are present in `application.yml`:

| Environment variable | Default | Purpose |
|---|---|---|
| `HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |

The configured connection and pool timeouts are set in the application configuration. In Docker Compose, set `HOST` to the Redis service name rather than `localhost`.

**Operational note:** Redis must be reachable by the backend for Redis-backed caching and rate limiting. Keep Redis private and do not publish its port to the public internet.

## Authentication and security

- **JWT access tokens** protect authenticated API operations.
- **Refresh-token flow** allows clients to obtain new access tokens without signing in on every request.
- **Google and GitHub OAuth** are supported through configured provider endpoints and client credentials.
- **CORS** is configurable through an environment variable.
- **Role-aware access and moderation** control privileged operations and user/listing visibility.
- **Request validation** uses Jakarta/Spring validation.
- **Rate limiting** helps constrain repeated requests to protected endpoints.

Relevant configuration variables:

| Environment variable | Default | Purpose |
|---|---|---|
| `JWT_SECRET` | Development placeholder | JWT signing secret; use a strong secret of at least 32 bytes |
| `JWT_EXPIRATION` | `900000` | Access-token lifetime in milliseconds (15 minutes) |
| `REFRESH_TOKEN_EXPIRATION` | `604800` | Refresh-token lifetime as configured by the application |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Allowed frontend origin(s) |
| `GOOGLE_CLIENT_ID` | Required for Google OAuth | OAuth client ID |
| `GOOGLE_CLIENT_SECRET` | Required for Google OAuth | OAuth client secret |
| `GITHUB_CLIENT_ID` | Required for GitHub OAuth | OAuth client ID |
| `GITHUB_CLIENT_SECRET` | Required for GitHub OAuth | OAuth client secret |
| `APP_FRONTEND_URL` | `http://localhost:3000` | Frontend URL used in OAuth flow |
| `APP_BACKEND_URL` | `http://localhost:8080` | Base URL used to construct OAuth callback URLs |
| `PROD` | `false` | Application production-mode flag |
| `DEMO_EMAIL` | No default shown | Email used for demo-account configuration |
| `DEV_ADMIN_PASSWORD` | Development password | Development/admin seeding configuration |
| `DEV_USER_PASSWORD` | Development password | Development/user seeding configuration |

Do not use development secrets or default passwords in a public deployment. Set all secrets through your deployment environment or secret manager, and configure OAuth callback URLs to match the deployed backend.

## Requirements

For local development, install:

- JDK 25
- Docker Engine and Docker Compose, or local instances of PostgreSQL and Redis
- An S3-compatible service (for example, a local S3 emulator) if testing image uploads
- Git

The repository includes the Maven Wrapper, so a separate Maven installation is not required.

## Configuration

The main configuration is in:

```text
src/main/resources/application.yml
```

Important settings include:

- PostgreSQL connection: `DB_URL`, `DB_USER`, `DB_PASSWORD`
- Redis connection: `HOST`, `REDIS_PORT`
- JWT: `JWT_SECRET`, `JWT_EXPIRATION`, `REFRESH_TOKEN_EXPIRATION`
- CORS: `CORS_ALLOWED_ORIGINS`
- OAuth: `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`
- OAuth URL construction: `APP_FRONTEND_URL`, `APP_BACKEND_URL`
- S3-compatible storage: `AWS_ENDPOINT`, `AWS_PUBLIC_ENDPOINT`, `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_DEFAULT_REGION`, `AWS_S3_BUCKET`, `AWS_S3_PATH_STYLE`

Database defaults in the checked-in configuration target a local PostgreSQL database at `localhost:5543/zero-waste-db`. Override these values if your local service uses another host, port, database, or credentials.

## Run locally

Clone the repository:

```bash
git clone https://github.com/Kamil-Mroz/zero-waste-backend.git
cd zero-waste-backend
```

Start PostgreSQL and Redis, then start an S3-compatible service if you need image functionality. Configure the environment variables described above.

Build and test:

```bash
./mvnw clean verify
```

Start the application:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

The server listens on port `8080` by default.

## Docker

Build the application:

```bash
./mvnw clean package -DskipTests
docker build -t zero-waste-backend .
```

Run the container with the required environment variables:

```bash
docker run --rm \
  --name zero-waste-backend \
  -p 8080:8080 \
  --env-file .env \
  zero-waste-backend
```

Create `.env` locally before using this command; do not commit it. Ensure that PostgreSQL, Redis, and the S3-compatible endpoint are reachable from inside the container. Do not use `localhost` for another container's service address.

## API documentation

With the application running, open:

- **Swagger UI:** `http://localhost:8080/api/v1/docs/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/api/v1/docs/api-docs`

The application configures path-segment API versioning, with version `1` as the default and versions `1` and `2` listed as supported. Check the live OpenAPI document for the exact routes, parameters, payload schemas, and response formats exposed by the current build.

## Database migrations

Flyway is enabled and reads migration scripts from:

```text
src/main/resources/db/migration
```

Hibernate is configured to validate the database schema rather than automatically create or modify it. This makes migrations the expected way to evolve the schema.

The Maven Flyway plugin reads `DB_URL`, `DB_USER`, and `DB_PASSWORD` from the environment. Configure these before running Flyway goals directly.

## Testing

Run the test suite:

```bash
./mvnw test
```

Or run the full Maven verification lifecycle:

```bash
./mvnw clean verify
```

The project includes H2 test support and Testcontainers dependencies for PostgreSQL-backed integration tests. Tests that use containers require a working Docker environment.

## Production notes

- Use HTTPS at the reverse proxy and keep PostgreSQL/Redis on private networks.
- Replace all development credentials and placeholder secrets.
- Set exact allowed frontend origins; avoid permissive CORS policies.
- Use a real S3-compatible endpoint and dedicated, least-privilege credentials.
- Configure bucket CORS for direct browser uploads.
- Treat presigned URLs as temporary bearer credentials.
- Back up PostgreSQL and object storage separately.
- Monitor application health and logs. The configuration exposes the health endpoint through Spring Boot's management endpoints.
- Keep API and object-store public URLs separate from internal Docker service addresses.

---

ZeroWaste is a portfolio project exploring modular Java backend development, API security, Redis-backed caching and rate limiting, and S3-compatible object storage with direct presigned uploads.
