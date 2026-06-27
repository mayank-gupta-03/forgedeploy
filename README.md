# ForgeDeploy 🚀

ForgeDeploy is a containerized, self-hosted deployment platform (inspired by Vercel) for web applications. It allows developers to trigger automated builds, package their projects, and serve them dynamically under wildcard subdomains.

---

## 🏗️ Tech Stack

- **Backend:** Spring Boot 4.0.6, Java 25, Spring Security (JWT & GitHub OAuth2), Flyway, Spring Data JPA.
- **Database:** PostgreSQL (persistent storage for users, projects, and deployments).
- **Object Storage:** MinIO (S3-compatible storage for source ZIPs and compiled static assets).
- **Reverse Proxy / Serving:** Nginx (handles wildcard subdomain routing and SPA fallbacks).
- **Build Isolation:** Docker API (executes builds inside isolated, ephemeral container environments).

---

## ⚡ Key Features (Built & Verified)

1. **Authentication & Authorization:** Dual authentication support via standard JWT login/registration endpoints and modern GitHub OAuth2 client integration.
2. **Project & Workspace Management:** Secure CRUD APIs for user projects with local workspace directories that clean up automatically when builds finish.
3. **Containerized Build Engine:** 
   - Supports **Node.js** projects (uses `node:20-alpine`, runs `npm ci` and builds assets).
   - Supports **Java/Maven** projects (uses `maven:3.9.16-eclipse-temurin-25-alpine` and packages apps with `mvn clean package -DskipTests`).
4. **Automated Serving Infrastructure:** Nginx maps incoming requests dynamically to `http://{projectId}--{deploymentId}.localhost` and proxies to MinIO with correct MIME type detection and Single Page Application (SPA) fallback routing.

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
To start the database, S3 storage, and Nginx reverse proxy, run:
```bash
docker compose up -d forgedeploy-db forge-minio forgedeploy-nginx
```
> **Note:** The `forgedeploy-service` is also defined in `docker-compose.yml` to run the fully containerized backend. However, starting only the database, storage, and proxy containers allows you to run and debug the Spring Boot backend directly from your host system or IDE during development.

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

## 🗺️ Roadmap & Short-Term TODOs

Please refer to the detailed implementation checklist in [.gemini/PLAN.md](file:///Users/mayankgupta/dev/forgedeploy/.gemini/PLAN.md) to track high-level roadmap items. 

### 📝 Short-Term TODOs

#### Phase 4: GitHub Integration
- [ ] Implement GitHub repository list & branch fetching using stored user OAuth tokens
- [ ] Add webhook endpoint to trigger automatic redeployment on git push

#### Phase 5: Dashboard UI
- [ ] Initialize React/TypeScript dashboard project in the `frontend` folder
- [ ] Build Login/Register screens and Project/Deployment Dashboard
- [ ] Develop real-time log viewer to stream container build output

#### Phase 6: Reliability & Polish
- [ ] Support custom domain mapping for deployments
- [ ] Add build environment variable configuration/secret injection
- [ ] Set resource constraints (CPU, memory limits) for build Docker containers
