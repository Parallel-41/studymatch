# StudyMatch

> Adaptive student group formation — Software Quality project (Universidade Portucalense, 2026/27)
> Team **Parallel 41**

## Product context

University students work in groups throughout their degree, but groups are usually formed randomly, by friendship or by availability. **StudyMatch** is a full-stack application that builds a meaningful representation of each student — academic trajectory, competencies and other evidence — and uses it to support automatic group formation in different academic contexts.

Two design problems are intentionally open and will be addressed by the team:

- **Cold start** — how to represent a student with little or no academic history.
- **Matching** — how to use history, competencies and other evidence to form groups as students progress.

The product evolves sprint by sprint; requirements are released incrementally.

## Team

| Member | GitHub | Sprint 2 role |
|---|---|---|
| Daniel | [@DanielRodrigues871](https://github.com/DanielRodrigues871) | Coordination, competency profile, CI |
| Guilherme | [@GuilhermeAla123321](https://github.com/GuilhermeAla123321) | Backend: students and academic history |
| Erzhan | [@eerrzhan](https://github.com/eerrzhan) | Frontend |
| Hugo | [@HugoGonzalez1](https://github.com/HugoGonzalez1) | Competency catalogue, demo data, use cases |
| Diego | [@MrNerga](https://github.com/MrNerga) | Domain model, self-assessment and cold start |

## Tech stack

React 19 + Vite + TypeScript (frontend) · Java 21 + Spring Boot 3.5 (backend) · MySQL 8.4 + Flyway · JUnit 5, Mockito, Testcontainers, Vitest. Rationale in [ADR-0001](docs/decisions/ADR-0001-technology-stack.md).

## Repository structure

```
StudyMatch/
├── frontend/                  # React + Vite + TypeScript client
│   └── src/api/examples/      # Shared JSON contracts (mocks for the frontend, structure checks for the backend)
├── backend/                   # Spring Boot API (package-by-feature: student, academic, competency, profile, common)
│   └── src/main/resources/db/migration/   # Flyway migrations (V1, V2, ...)
├── docs/
│   ├── decisions/             # Architecture Decision Records (ADRs)
│   ├── use-cases/             # Use-case model
│   ├── domain/                # Domain model (UML) and business rules
│   ├── architecture/          # Architecture diagram + explanation
│   ├── CODING_CONVENTIONS.md
│   └── DEFINITION_OF_DONE.md
├── docker-compose.yml         # Local MySQL 8.4
├── .env.example               # Database settings used by docker compose
├── scripts/                   # Repo automation (labels, milestones, issues)
├── .github/                   # CI workflow, issue and PR templates
└── CONTRIBUTING.md            # Team workflow (branches, PRs, reviews)
```

## Getting started

These steps were checked on a clean clone. Commands are run from the repository root unless stated otherwise.

### Prerequisites

| Tool | Version | Check |
|---|---|---|
| Git | any recent | `git --version` |
| JDK | **21** (`JAVA_HOME` must point to it) | `java -version` |
| Node.js | **20 or later** (CI uses 22) | `node -v` |
| Docker | Docker Desktop or OrbStack, **running** | `docker version` |

Maven is **not** needed: the backend uses the Maven Wrapper (`./mvnw` on macOS/Linux, `.\mvnw.cmd` on Windows).

### 1. Clone

```bash
git clone https://github.com/Parallel-41/studymatch.git
cd studymatch
```

### 2. Start the database

```bash
cp .env.example .env          # optional: change the passwords
docker compose up -d
docker compose ps             # mysql must be "healthy"
```

MySQL listens on `localhost:3306`, database `studymatch`, user `studymatch`. The backend reads `DB_NAME`, `DB_USER` and `DB_PASSWORD` and uses the same defaults as `.env.example`, so no extra configuration is needed. If you change the password in `.env`, export the same `DB_PASSWORD` before starting the backend.

### 3. Run the backend

```bash
cd backend
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run
```

On start-up Flyway creates the schema and the demo data. The API runs on **http://localhost:8080**. Check it with:

```bash
curl http://localhost:8080/api/health
```

### 4. Run the frontend

In a second terminal:

```bash
cd frontend
npm ci
npm run dev
```

Open **http://localhost:5173**. Vite forwards every `/api` request to the backend on port 8080, so there is no CORS setup. If the backend is not running, the pages fall back to the mock data in `src/api/examples/`.

## Testing

### Backend

```bash
cd backend
./mvnw clean verify           # Windows: .\mvnw.cmd clean verify
```

- Runs the unit tests (JUnit 5, Mockito), the controller tests (`@WebMvcTest`) and the integration tests (Testcontainers + MySQL 8.4 with the real Flyway migrations).
- **Docker must be running** for the integration tests. Without Docker they are skipped, not failed.
- The coverage report is written to `backend/target/site/jacoco/index.html`.

### Frontend

```bash
cd frontend
npm test                      # Vitest
npm run lint                  # oxlint
npm run build                 # type check + production build
```

### Continuous integration

Every pull request runs `.github/workflows/ci.yml`: the **backend** job (`./mvnw -B clean verify`, JaCoCo report as an artifact) and the **frontend** job (`npm ci`, lint, test, build). Both checks must be green before a PR can be merged into `main`.

## API

| Method | Path | Description |
|---|---|---|
| GET | `/api/health` | Service status |
| GET | `/api/students` | List students |
| GET | `/api/students/{id}` | One student |
| POST | `/api/students` | Create a student (body: `frontend/src/api/examples/student-request.json`) |
| PUT | `/api/students/{id}` | Update a student |
| GET | `/api/students/{id}/history` | Academic history grouped by academic year and semester |
| POST | `/api/students/{id}/attempts` | Register an assessment attempt |
| GET | `/api/course-units` | Course units |
| GET | `/api/competencies` | Competency catalogue grouped by area |
| GET | `/api/students/{id}/profile` | Competency profile, computed on every request ([ADR-0003](docs/decisions/ADR-0003-profile-and-cold-start.md)) |

Errors use one format (`ApiError`): `400` invalid data, `404` not found, `409` conflict. Example responses for every endpoint live in [`frontend/src/api/examples/`](frontend/src/api/examples/).

## Troubleshooting

| Problem | Fix |
|---|---|
| `permission denied: ./mvnw` | `chmod +x backend/mvnw`, or run `sh mvnw ...` |
| `Validate failed: Migrations have failed validation` / checksum mismatch | The local database was created with older migrations. Reset it: `docker compose down -v && docker compose up -d` |
| Port `3306` already in use | Stop the local MySQL service, or change the host port in `docker-compose.yml` and in `spring.datasource.url` (`backend/src/main/resources/application.yml`) |
| Testcontainers: `Could not find a valid Docker environment` | Start Docker Desktop / OrbStack and run the tests again |
| Backend starts but the frontend shows mock data | Check that the backend is on port 8080 and `curl http://localhost:8080/api/health` answers |

## How we work

Every change follows **Issue → Branch → Implementation → Pull Request → Review → Merge**.
Read [CONTRIBUTING.md](CONTRIBUTING.md) before opening your first branch.

## Documentation

- [Contributing / workflow](CONTRIBUTING.md)
- [Definition of Done](docs/DEFINITION_OF_DONE.md)
- [Coding conventions](docs/CODING_CONVENTIONS.md)
- [Architecture decisions](docs/decisions/) — ADR-0001 stack, ADR-0003 profile and cold start
- [Use-case model](docs/use-cases/)
- [Domain model](docs/domain/)
- [Architecture](docs/architecture/)

## Sprint status

| Sprint | Focus | Milestone |
|---|---|---|
| 1 | Project foundation — workflow, use cases, domain, architecture, full-stack skeleton | `Sprint 1` |
| 2 | Academic history, competency profile, cold start, profile explanation, CI | `Sprint 2` |
