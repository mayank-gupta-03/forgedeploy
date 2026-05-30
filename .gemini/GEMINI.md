# GEMINI.md - ForgeDeploy Project Context

## Project Overview
ForgeDeploy is a Vercel-like deployment platform designed for simplicity and efficiency. It enables users to manage projects and deploy applications via GitHub integration or direct ZIP uploads. The system is designed around an asynchronous processing engine that builds and isolates code in Docker containers.

## Technology Stack
- **Backend:** Java 25, Spring Boot 4.0.x, Spring Security (JWT), Spring Data JPA
- **Database:** PostgreSQL 16 (via Docker)
- **Migrations:** Flyway
- **Infrastructure:** Docker, NGINX (Reverse Proxy), MinIO (S3 Compatible Storage)
- **Authentication:** Stateless JWT with BCrypt encoding.

## Project Structure
- `backend/`: Spring Boot application containing API and core deployment logic.
- `frontend/`: (Placeholder) Future dashboard and user interface.
- `infrastructure/`: Infrastructure-related configurations.
- `nginx/`: NGINX configuration for routing and serving assets.
- `docker-compose.yml`: Local development environment setup (Postgres, MinIO, Nginx).

## Building and Running

### Prerequisites
- Java 25
- Docker and Docker Compose
- Maven (via `./mvnw`)

### Local Infrastructure
Spin up the required services (Database, Object Storage, Proxy):
```bash
docker-compose up -d
```

### Backend Application
Run the Spring Boot application:
```bash
cd backend
./mvnw spring-boot:run
```

### Database Migrations
Flyway migrations are located in `backend/src/main/resources/db/migration`. They run automatically on application startup.

## Development Conventions

### API Guidelines
- **Versioning:** All API endpoints MUST be prefixed with `/api/v1`.
- **Security:** Use JWT for authentication. All `/api/v1/**` endpoints are secured except for registration and login.
- **DTOs:** Use Data Transfer Objects (DTOs) for all API requests and responses.

### Code Style
- **Lombok:** Extensively used for boilerplate reduction (Getter, Setter, Slf4j, etc.).
- **Exception Handling:** Centralized in `com.forgedeploy.service.common.exception.GlobalExceptionHandler`.
- **UUIDs:** Use `UUID` for all primary keys in the database for scalability and security.

### Testing
- Backend tests are located in `backend/src/test/java`.
- Run tests using: `./mvnw test`

## Roadmap & Core Logic
- **Async Processing:** Uses a poll-based queue (Spring `@Scheduled`) to pick up `QUEUED` jobs.
- **Isolation:** Each deployment gets a dedicated workspace directory and runs inside a Docker container for builds.
- **Artifacts:** Successfully built assets are uploaded to MinIO and served via a dynamic NGINX proxy.
