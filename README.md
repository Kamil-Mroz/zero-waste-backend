# Zero Waste Backend

Spring Boot API for the Zero Waste community platform — a peer-to-peer item sharing and trading application.

## Summary

The backend provides authentication, item management, real-time notifications, reviews, blogging, and user profiles. It uses JWT for auth, WebSocket for live updates, and PostgreSQL for persistence.

## Tech Stack

| Technology | Version / Purpose |
|---|---|
| Java | 25 |
| Spring Boot | 4.0.5 |
| PostgreSQL | Runtime database |
| H2 | Test database |
| Spring Data JPA | ORM |
| Spring Security | OAuth2 resource server + JWT |
| WebSocket (STOMP) | Real-time notifications |
| SpringDoc OpenAPI | Swagger UI (`/api/docs/swagger-ui.html`) |
| MapStruct | DTO mapping |
| Lombok | Boilerplate reduction |

## Prerequisites

- Java 25 (Amazon Corretto recommended)
- Maven 3.9+
- PostgreSQL 14+ (or use Docker)

## Installation

```bash
cd zero-waste-backend
./mvnw clean install
```

## Configuration

Environment variables (defaults in parentheses):

| Variable | Default | Description |
|---|---|---|
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5543` | PostgreSQL port |
| `DB_NAME` | `zero-waste-db` | Database name |
| `DB_USER` | `zero-waste-user` | Database user |
| `DB_PASSWORD` | `zero-waste-password` | Database password |
| `JWT_SECRET` | *(dev value)* | JWT signing secret (min 32 bytes) |
| `JWT_EXPIRATION` | `900000` | Access token TTL (ms) |
| `REFRESH_TOKEN_EXPIRATION` | `604800` | Refresh token TTL (ms) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Allowed CORS origins |
| `DEV_PASSWORD` | `SecurePassword123!` | Seeder admin password |

## Running

### Locally

```bash
./mvnw spring-boot:run
```

Server starts on port **8080**.

### Docker

```bash
./mvnw package -DskipTests
docker build -t zero-waste-backend .
docker run -p 8080:8080 zero-waste-backend
```

## API Endpoints

| Controller | Endpoints |
|---|---|
| **Auth** | Register, login, refresh token |
| **User** | User CRUD, ban/unban |
| **Profile** | Own profile, public user profile |
| **Item** | Create, update, list items |
| **Category** | Category tree, CRUD |
| **Review** | Create, list, rating breakdown |
| **Offer** | Create, manage trade offers |
| **Blog** | Blog post CRUD |
| **Image** | Image upload/storage |
| **Notification** | WebSocket + REST notifications |

Full API docs: `http://localhost:8080/api/docs/swagger-ui.html`

## Project Structure

```
src/main/java/com/kamilpm/zero_waste/
├── annotation/          # Custom validation annotations
├── config/              # Security, WebSocket, CORS, seeder
├── controller/          # REST controllers
├── domain/
│   ├── dto/             # Data transfer objects
│   ├── entity/          # JPA entities
│   ├── mapper/          # MapStruct mappers
│   ├── request/         # Request DTOs
│   └── response/        # Response DTOs
├── exception/           # Custom exceptions
├── filters/             # JWT filter
├── interceptors/        # WebSocket auth interceptor
├── repository/          # Spring Data repositories
├── security/            # JWT utilities, auth entry point
├── service/             # Business logic services
└── validation/          # Validation utilities
```

## Testing

```bash
./mvnw test
```

## API Versioning

- Default: `v1`
- Supported: `v1`, `v2`
- Strategy: path segment (`/api/v1/...`)
