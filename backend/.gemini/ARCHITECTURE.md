# ForgeDeploy Architecture

This document describes the high-level architecture and component interactions of the ForgeDeploy platform.

## Architecture Diagram

```mermaid
graph TD
    subgraph "Client Side"
        User((User))
        Browser[Frontend Dashboard - React]
    end

    subgraph "Infrastructure & Routing"
        Nginx[Nginx Reverse Proxy]
        Wildcard[*.localhost Dynamic Serving]
    end

    subgraph "Backend Services (Spring Boot)"
        API[REST Controllers: Auth, Project, Deployment]
        AuthS[Auth Service - JWT]
        ProjS[Project Service]
        DeplS[Deployment Service]
        S3S[S3 Service - MinIO Integration]
        
        subgraph "Async Execution Engine"
            Queue[Deployment Queue Processor - @Scheduled]
            Executor[Async Thread Pool - Executor]
        end
    end

    subgraph "Storage & Data"
        DB[(PostgreSQL 16)]
        MinIO[(MinIO Object Storage)]
    end

    subgraph "Build Engine (Planned Phase 2)"
        Workspace[/Local Temp Workspace/]
        Docker[Docker Build Containers - Node/Python]
    end

    subgraph "External"
        GitHub[GitHub API & Webhooks]
    end

    %% Flow: Management
    User -->|Manage Projects| Nginx
    Nginx -->|Proxy| Browser
    Browser -->|API Calls| API
    API --> ProjS
    API --> DeplS
    ProjS <--> DB
    DeplS <--> DB

    %% Flow: Upload
    User -->|Upload ZIP| API
    DeplS -->|Store Source| S3S
    S3S -->|Upload| MinIO

    %% Flow: Build Process
    Queue -->|Poll QUEUED Jobs| DB
    Queue -->|Dispatch| Executor
    Executor -->|1. Setup| Workspace
    S3S -.->|2. Download Source| Workspace
    GitHub -.->|2. Clone Repo| Workspace
    Executor -->|3. Run Build| Docker
    Docker -->|Mounts| Workspace
    Docker -->|Stream Logs| DeplS
    Executor -->|4. Upload Artifacts| S3S
    S3S -->|Final Build| MinIO

    %% Flow: Serving
    User -->|Access site.localhost| Wildcard
    Wildcard -->|Fetch Assets| MinIO
    Wildcard -->|Serve| User
```

## Component Breakdown

### 1. Nginx (Gateway)

* Acts as the entry point for both the management dashboard and the deployed user sites.
* **Phase 3** will configure it to handle wildcard subdomains (`*.localhost`) to route traffic to specific deployments.

### 2. Spring Boot Backend

* **Controllers:** Handle the "Control Plane"—creating projects, triggering deployments, and user auth.
* **DeploymentQueueProcessor:** A background worker that prevents the API from freezing during long builds. It polls the
  database every 5 seconds for new jobs.
* **S3 Service:** The bridge to MinIO. It handles everything from storing initial ZIP uploads to saving the final
  production-ready artifacts.

### 3. PostgreSQL

* Stores metadata: User accounts, Project settings, Deployment history, and build durations.

### 4. MinIO (The "Warehouse")

* Stores the "Data Plane": The raw source code ZIPs and the final bundled `dist` or `build` folders.

### 5. Build Engine (Phase 2)

* **Workspace:** A temporary folder on the server where the code is extracted/cloned.
* **Docker:** Used to isolate the build. If a user runs a malicious build script, it only affects the short-lived
  container, not your main server.

### 6. GitHub Integration (Phase 4)

* Automates the flow so that a `git push` triggers the backend to start the cycle automatically.
