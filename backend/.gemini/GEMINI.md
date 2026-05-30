# GEMINI.md - ForgeDeploy Backend (Service)

## Project Overview
This is the core backend service for ForgeDeploy, a Vercel-like deployment platform. It handles user authentication, project management, and orchestrates the deployment lifecycle.

## Technology Stack
- **Language:** Java 25
- **Framework:** Spring Boot 4.0.6
- **Security:** Spring Security with stateless JWT (using JJWT)
- **Database:** PostgreSQL (Primary storage)
- **Migrations:** Flyway
- **Build Tool:** Maven (via `./mvnw`)
- **Libraries:** Lombok (boilerplate reduction), Jakarta Validation

## Key Components & Architecture
- **API Versioning:** Core API endpoints are prefixed with `/api/v1/`. Authentication endpoints are under `/auth/`.
- **Modular Structure:** Code is organized by domain modules under `com.forgedeploy.service.modules`:
    - `auth`: Registration and login logic.
    - `projects`: Project management (creation, retrieval).
    - `deployments`: Deployment job creation and status tracking.
    - `users`: User repository and identity management.
- **Security:** 
    - `JwtService`: Handles token generation and validation.
    - `JwtAuthFilter`: Intercepts requests to validate JWTs.
    - `UserPrincipal`: Custom implementation of `UserDetails`.
- **Exception Handling:** Centralized in `GlobalExceptionHandler` using `@ControllerAdvice`.
- **Entities:** Uses JPA with UUID primary keys for all major entities (`UserInfo`, `Project`, `Deployment`, `DeploymentJob`).

## Building and Running

### Prerequisites
- JDK 25
- PostgreSQL (configured in `application.yaml`)

### Commands
- **Run Application:** `./mvnw spring-boot:run`
- **Build JAR:** `./mvnw clean package`
- **Run Tests:** `./mvnw test`
- **Run Flyway Migrations:** Migrations run automatically on startup.

## Development Conventions
- **DTOs:** Always use DTOs for requests and responses. Avoid exposing JPA entities directly.
- **Validation:** Use Jakarta Validation annotations (e.g., `@NotBlank`, `@Email`) in DTOs.
- **Lombok:** Use `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`, and `@Slf4j` as needed.
- **Error Handling:** Define specific exceptions for business logic errors and handle them in `GlobalExceptionHandler`.
- **Database:** All primary keys MUST be `UUID`. Use `@GeneratedValue(strategy = GenerationType.UUID)` in entities.

## Future Roadmap (Inferred)
- **Async Build Engine:** Implement a worker to process `QUEUED` deployments.
- **Docker Integration:** Logic to spawn build containers.
- **S3 Integration:** Uploading built artifacts to object storage.
