# ForgeDeploy Implementation Plan

ForgeDeploy is a Vercel-like platform for deploying web applications. This plan outlines the steps required to complete the project, moving from the current foundation to a fully functional MVP.

## Phase 1: Storage & Job Queue (Backend Core) - COMPLETED
- [x] **1.1 MinIO Integration:** Added S3 support for artifact storage.
- [x] **1.2 Job Repository Refinement:** Updated `Deployment` entity with granular statuses and log paths.
- [x] **1.3 Async Job Processor:** Implemented `@Scheduled` polling and `@Async` worker thread pool.

## Phase 2: Build Engine (Isolation & Execution) - IN PROGRESS
- [x] **2.1 Workspace Management:** Logic to create temporary directories and extract source code.
- [ ] **2.2 Docker Build Runner (Next):** Implementing containerized builds using Docker API.
- [ ] **2.3 Artifact Extraction:** Extracting and uploading build output (e.g., `dist/`) to MinIO.

## Phase 3: Dynamic Serving (Infrastructure)
- [ ] **3.1 Nginx Dynamic Proxy:** Wildcard subdomain handling (`*.localhost`).
- [ ] **3.2 Routing Logic:** Subdomain to deployment artifact mapping.

## Phase 4: GitHub Integration
- [ ] **4.1 GitHub OAuth:** User account linking.
- [ ] **4.2 Repository Browser:** Listing user repos and branches.
- [ ] **4.3 Webhook Support:** Automatic deployment on push.

## Phase 5: Frontend Development (Dashboard)
- [ ] **5.1 Setup:** Initialize React/TypeScript project.
- [ ] **5.2 Authentication:** UI for Login/Register.
- [ ] **5.3 Project Dashboard:** Project list and details.
- [ ] **5.4 Deployment Logs:** Real-time log viewer.
- [ ] **5.5 Deployment Wizard:** Project creation flow.

## Phase 6: Reliability & Polish
- [ ] **6.1 Resource Constraints:** Build container limits.
- [ ] **6.2 Cleanup Logic:** Automated workspace and image cleanup.
- [ ] **6.3 Environment Variables:** Secret injection for builds.
- [ ] **6.4 Custom Domains:** User-defined domain mapping.
