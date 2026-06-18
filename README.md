# ForgeDeploy 🚀

ForgeDeploy is a containerized, self-hosted deployment platform (inspired by Vercel) for web applications. It allows developers to trigger automated builds, package their projects, and serve them dynamically under wildcard subdomains.

---

## 🏗️ Tech Stack

- **Backend:** Spring Boot 4.0.6, Java 25, Spring Security (JWT-based), Flyway, Spring Data JPA.
- **Database:** PostgreSQL (user, project, and deployment/job persistence).
- **Object Storage:** MinIO (S3-compatible object storage for source ZIPs and static artifacts).
- **Reverse Proxy / Serving:** Nginx (handles wildcard subdomain routing and SPA fallbacks).
- **Build Isolation:** Docker API (runs build commands in isolated, ephemeral containers).

---

## ⚡ Key Features (Built & Verified)

1. **Authentication & Authorization:** Secure JWT-based authentication for user registration and logins.
2. **Project & Workspace Management:** CRUD APIs to manage user projects and ephemeral directories that automatically clean up after builds.
3. **Containerized Build Engine:** 
   - Supports **Node.js** projects (uses `node:20-alpine`, installs dependencies with `npm ci`, and builds).
   - Supports **Java/Maven** projects (uses `maven:3.9.16-eclipse-temurin-25-alpine` and packages apps with `mvn clean package -DskipTests`).
4. **Automated Serving Infrastructure:** Nginx maps requests to `http://{projectId}--{deploymentId}.localhost` and fetches static assets directly from MinIO with automatic MIME type detection and Single Page Application (SPA) fallback routing.

---

## 📂 Project Structure

```
├── .gemini/               # Agent planning files (e.g. PLAN.md)
├── backend/               # Spring Boot 4.0.6 / Java 25 Service
│   ├── src/               # Application source code
│   └── pom.xml            # Maven project descriptor
├── nginx/                 # Serving configuration
│   └── nginx.conf         # Wildcard reverse proxy rules
├── docker-compose.yml     # Local database, object storage, and reverse proxy definition
├── ARCHITECTURE_FLOW.md   # Deep-dive system flow and component responsibilities
└── README.md              # Main entry documentation
```

For a detailed walkthrough of the system flow, component design, and resource life cycle, see the [Architecture Flow Documentation](file:///Users/mayankgupta/dev/forgedeploy/ARCHITECTURE_FLOW.md).

---

## 🚦 Getting Started

### Prerequisites
Make sure you have the following installed on your machine:
- **Docker** and **Docker Compose**
- **Java 25** and **Maven** (to run the backend service on your host)

---

### Step 1: Start the Infrastructure
Use Docker Compose to launch PostgreSQL, MinIO, and Nginx in the background:
```bash
docker compose up -d
```
> **Note:** The `forgedeploy-service` is defined in `docker-compose.yml` but remains commented out. This configuration allows you to run and debug the Spring Boot backend directly from your IDE or host system during development.

---

### Step 2: Start the Backend Service
Navigate to the `backend` directory and run the Spring Boot application:
```bash
cd backend
./mvnw spring-boot:run
```

Once running, the application will initialize the S3 bucket policies and run migrations via Flyway. The service will be available at `http://localhost:8080`.

---

### Step 3: Trigger a Deployment
You can test the deployment pipeline using the REST APIs:

1. **Register a User:**
   ```bash
   curl -X POST http://localhost:8080/auth/register \
     -H "Content-Type: application/json" \
     -d '{"email": "user@example.com", "password": "password123"}'
   ```

2. **Login:**
   ```bash
   curl -X POST http://localhost:8080/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email": "user@example.com", "password": "password123"}'
   ```
   *Copy the `token` from the response.*

3. **Create a Project:**
   ```bash
   curl -X POST http://localhost:8080/api/v1/projects \
     -H "Authorization: Bearer <TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{"name": "my-spa-project"}'
   ```
   *Copy the project `id`.*

4. **Upload and Deploy a ZIP:**
   Send a `POST` request with your project files packaged in a ZIP archive.
   ```bash
   curl -X POST http://localhost:8080/api/v1/deployments \
     -H "Authorization: Bearer <TOKEN>" \
     -F "projectId=<PROJECT_ID>" \
     -F "sourceType=ZIP" \
     -F "projectType=NODE" \
     -F "file=@/path/to/your/project-source.zip"
   ```

5. **Track Deployment Progress:**
   Check the deployment status using the deployment `id` returned from the upload request:
   ```bash
   curl -H "Authorization: Bearer <TOKEN>" \
     http://localhost:8080/api/v1/deployments/<DEPLOYMENT_ID>
   ```
   The status will transition from `QUEUED` ➔ `CLONING` ➔ `BUILDING` ➔ `UPLOADING` ➔ `COMPLETED`.

---

### Step 4: Access Your Deployed Site
Once the deployment status reaches `COMPLETED`, you can access your deployed static site at:
```
http://{projectId}--{deploymentId}.localhost
```
Nginx automatically resolves this pattern and routes the request to serve the compiled artifacts directly from the storage bucket.

---

## 🗺️ Roadmap & Next Steps

Please refer to the updated implementation checklist in [.gemini/PLAN.md](file:///Users/mayankgupta/dev/forgedeploy/.gemini/PLAN.md) to track completed phases and future goals, including:
- **Phase 4:** GitHub Integration (OAuth, Repo browsing, Webhook triggers)
- **Phase 5:** React/TypeScript Web Dashboard (Authentication, real-time log viewers)
- **Phase 6:** Advanced features (custom domains, environment secret injection, and build container resource limits)
