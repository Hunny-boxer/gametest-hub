
# GameTest Hub — System Architecture

**Project:** GameTest Hub  
**Architecture:** Modular Monolith  
**Status:** Architecture baseline  
**Owner:** Hunny Boxer

---

## 1. Project Overview

GameTest Hub is an enterprise-grade Gaming QA Management Platform designed to manage the complete software testing lifecycle of gaming projects.

The platform will help QA teams manage:

- Game projects and builds.
- Test cases and test suites.
- Test plans and assignments.
- Manual and automated test execution.
- Bug and defect tracking.
- Build-to-build quality comparisons.
- QA analytics and reporting.
- AI-assisted test analysis.

The system will initially use a modular monolith architecture with clearly separated business domains.

---

## 2. Technology Stack

| Layer | Technology |
|---|---|
| Frontend | React + TypeScript |
| Frontend Tooling | Vite |
| Backend | Java + Spring Boot |
| Database | PostgreSQL |
| ORM | Spring Data JPA |
| Security | Spring Security |
| Database Migration | Flyway |
| API Documentation | OpenAPI / Swagger |
| Backend Testing | JUnit 5 |
| Integration Testing | Testcontainers |
| Frontend Testing | Vitest |
| E2E Testing | Playwright |
| CI/CD | GitHub Actions |
| Containerization | Docker |
| AI Integration | Gemini API (planned) |

Versions and dependencies will be pinned in the application build files.

---

## 3. High-Level Architecture

```text
                 QA ENGINEERS / ADMINS
                           |
                           v
                REACT FRONTEND
                 React + TypeScript
                           |
                           v
                    REST API
                  Spring Boot
                           |
             +-------------+-------------+
             |             |             |
             v             v             v
        SECURITY       DOMAIN         SERVICES
        MODULES        MODULES
             |             |
             |       +-----+------+
             |       |     |      |
             |       v     v      v
             |    PROJECT TEST   DEFECT
             |    MODULE  MODULE MODULE
             |       |     |      |
             +-------+-----+------+
                           |
                           v
                      POSTGRESQL
                           |
                  +--------+--------+
                  |                 |
                  v                 v
             FILE STORAGE      ANALYTICS
```

The backend owns all business rules, authentication,
authorization, validation, and database operations.

The frontend communicates with the backend exclusively
through documented APIs.

---

## 4. Backend Modules

### 4.1 Authentication and Authorization

Responsibilities:

- User authentication.
- Session and token validation.
- Role-based access control.
- Workspace membership.
- Project-level permissions.
- Secure API access.

Initial roles:

| Role | Permissions |
|---|---|
| OWNER | Full workspace control |
| ADMIN | Workspace and member administration |
| QA_LEAD | Manage projects, test plans and executions |
| QA_ENGINEER | Create test cases, execute tests and report defects |
| VIEWER | Read-only access |

All permissions must be enforced on the backend.

### 4.2 Workspace Management

Responsibilities:

- Create workspaces.
- Manage workspace members.
- Assign roles.
- Configure workspace settings.
- Maintain tenant boundaries.

A workspace represents an organization or team.

### 4.3 Project Management

Responsibilities:

- Create gaming projects.
- Manage project information.
- Configure supported platforms.
- Manage project members.
- Track project activity.

### 4.4 Build Management

Responsibilities:

- Register new game builds.
- Store version information.
- Track build numbers.
- Maintain release metadata.
- Associate builds with test executions.
- Compare quality between builds.

Build information should include:

- Build name.
- Version.
- Build number.
- Platform.
- Environment.
- Source revision.
- Release status.
- Creation timestamp.

### 4.5 Test Case Management

Responsibilities:

- Create test cases.
- Organize test steps.
- Assign priorities.
- Manage test suites.
- Maintain reusable test cases.
- Track test case versions.

Initial test case statuses:

- DRAFT
- ACTIVE
- ARCHIVED

### 4.6 Test Plan Management

Responsibilities:

- Create test plans.
- Associate test suites.
- Select target builds.
- Assign QA engineers.
- Configure execution scope.
- Track plan progress.

### 4.7 Test Execution Engine

Responsibilities:

- Create test runs.
- Execute manual test cases.
- Import automated test results.
- Record execution duration.
- Store test evidence.
- Track historical results.

Initial execution statuses:

- NOT_RUN
- PASSED
- FAILED
- BLOCKED
- SKIPPED

The system should support JUnit XML and TestNG report ingestion.

### 4.8 Defect Management

Responsibilities:

- Create defects.
- Assign severity and priority.
- Record reproduction steps.
- Attach screenshots and logs.
- Associate defects with test results.
- Track resolution status.

Initial defect statuses:

- OPEN
- IN_PROGRESS
- FIXED
- READY_FOR_RETEST
- REOPENED
- CLOSED
- WONT_FIX

### 4.9 QA Analytics

Responsibilities:

- Calculate test pass rates.
- Track failure trends.
- Compare builds.
- Monitor execution duration.
- Identify flaky tests.
- Generate project-level reports.
- Track release readiness.

### 4.10 Audit Management

Responsibilities:

- Record important user actions.
- Track configuration changes.
- Track membership changes.
- Record test execution events.
- Maintain defect activity history.

Sensitive credentials and tokens must never be written to audit logs.

---

## 5. Frontend Modules

The frontend will be organized into feature-based modules.

```text
frontend/src/
│
├── app/
│   ├── App.tsx
│   ├── routes.tsx
│   └── providers/
│
├── components/
│   ├── layout/
│   ├── navigation/
│   ├── forms/
│   ├── tables/
│   └── charts/
│
├── features/
│   ├── authentication/
│   ├── dashboard/
│   ├── workspaces/
│   ├── projects/
│   ├── builds/
│   ├── test-cases/
│   ├── test-plans/
│   ├── executions/
│   ├── defects/
│   └── analytics/
│
├── hooks/
├── services/
├── types/
├── utils/
├── styles/
└── main.tsx
```

Each feature will own its UI components, API integration, types and tests.

---

## 6. Database Architecture

PostgreSQL will be the primary source of truth.

Initial conceptual entities:

| Entity | Purpose |
|---|---|
| users | Application users |
| workspaces | Tenant organizations |
| workspace_members | Workspace membership and roles |
| projects | Gaming projects |
| platforms | Supported gaming platforms |
| environments | Testing environments |
| builds | Game build metadata |
| test_cases | Reusable test definitions |
| test_steps | Ordered test instructions |
| test_suites | Groups of test cases |
| test_plans | Planned testing activities |
| test_runs | Execution sessions |
| test_results | Individual test outcomes |
| defects | Reported bugs |
| attachments | Evidence metadata |
| audit_events | Important activity history |

### Important relationships

```text
WORKSPACE
    |
    +---- WORKSPACE_MEMBERS
    |
    +---- PROJECT
              |
              +---- BUILDS
              |
              +---- TEST_CASES
              |         |
              |         +---- TEST_STEPS
              |
              +---- TEST_SUITES
              |
              +---- TEST_PLANS
              |
              +---- TEST_RUNS
                        |
                        +---- TEST_RESULTS
                        |
                        +---- DEFECTS
```

All workspace-owned resources must be scoped
to their owning workspace and project.

Database foreign keys and backend authorization
must prevent cross-workspace data access.

Flyway will manage database schema migrations.

---

## 7. API Architecture

All application endpoints will use the prefix:

`/api/v1`

Initial planned endpoints:

| Method | Endpoint | Purpose |
|---|---|---|
| GET | /api/v1/me | Current user |
| GET | /api/v1/workspaces | List workspaces |
| POST | /api/v1/workspaces | Create workspace |
| GET | /api/v1/projects | List projects |
| POST | /api/v1/projects | Create project |
| GET | /api/v1/projects/{id}/builds | List builds |
| POST | /api/v1/projects/{id}/builds | Register build |
| GET | /api/v1/projects/{id}/test-cases | List test cases |
| POST | /api/v1/projects/{id}/test-cases | Create test case |
| POST | /api/v1/projects/{id}/test-runs | Create test run |
| POST | /api/v1/projects/{id}/defects | Report defect |
| GET | /api/v1/projects/{id}/analytics | QA analytics |

These are planned API contracts, not implemented endpoints.

---

## 8. Security Architecture

Security is a core requirement.

- Authentication is required for protected APIs.
- Access permissions must be verified on the backend.
- Workspace membership controls tenant access.
- Project access must be checked for every project resource.
- Database queries must be scoped to authorized resources.
- Secrets must be stored outside source control.
- Passwords must be securely hashed if locally managed.
- File uploads must have size and type restrictions.
- API inputs must be validated.
- CORS must use explicit allowed origins.
- Security-sensitive actions must be audited.

Never rely on frontend visibility or client-supplied
workspace identifiers as an authorization mechanism.

---

## 9. Testing Architecture

### Backend Testing

- Unit tests.
- Service-layer tests.
- Repository tests.
- REST API tests.
- Authentication tests.
- Authorization tests.
- Database integration tests.

### Frontend Testing

- Component tests.
- UI interaction tests.
- API integration tests.
- Form validation tests.
- End-to-end browser tests.

### Gaming QA Integration Testing

The platform should test its own test management capabilities.

Important scenarios include:

- Create a gaming project.
- Register a build.
- Create test cases.
- Execute a test plan.
- Import automated test results.
- Report a defect.
- Compare two build results.
- Verify workspace isolation.

---

## 10. CI/CD Architecture

GitHub Actions will initially run:

```text
                 GITHUB PUSH
                      |
                      v
                 BUILD SOURCE
                      |
             +--------+--------+
             |                 |
             v                 v
        BACKEND CI         FRONTEND CI
             |                 |
             v                 v
        UNIT TESTS         UI TESTS
             |                 |
             v                 v
        API TESTS          BUILD CHECK
             |                 |
             +--------+--------+
                      |
                      v
                 FINAL STATUS
```

Only successfully validated changes should proceed
to deployment.

---

## 11. Development Roadmap

Phase 1: Repository and architecture foundation.

Phase 2: Backend and frontend scaffolding.

Phase 3: Authentication and workspace management.

Phase 4: Gaming project and build management.

Phase 5: Test case and test plan management.

Phase 6: Manual test execution.

Phase 7: Automated report ingestion.

Phase 8: Defect management.

Phase 9: QA analytics and build comparison.

Phase 10: Flaky test intelligence.

Phase 11: AI-assisted QA capabilities.

Phase 12: Production deployment and monitoring.

---

## 12. Engineering Standards

- Maintain clear separation of responsibilities.
- Prefer modular, maintainable code.
- Use consistent naming conventions.
- Write automated tests for business logic.
- Document important API contracts.
- Maintain database migrations.
- Avoid hardcoded credentials.
- Review dependencies before upgrades.
- Keep development, testing and production configuration separate.

---

## 13. Architecture Decisions

| Decision | Selected approach |
|---|---|
| Backend architecture | Modular monolith |
| Frontend architecture | Feature-based React application |
| Database | PostgreSQL |
| API style | REST |
| API prefix | /api/v1 |
| Schema migrations | Flyway |
| Authorization | Backend-enforced RBAC |
| Automated testing | JUnit, Testcontainers and Playwright |
| CI/CD | GitHub Actions |

Microservices, advanced AI processing and distributed
job infrastructure are deferred until justified
by actual product requirements.

---

**GameTest Hub**  
Built by Hunny Boxer.
