# ADR-0001 — Technology stack

- **Status:** Accepted — accepted by the team in PR #32 (2026-10-14)
- **Date:** 2026-10-06
- **Deciders:** Daniel, Guilherme, Erzhan, Hugo, Diego
- **Issue:** #5

## Context
StudyMatch must become a maintainable, testable full-stack application (frontend, API, business logic, persistence) whose requirements change every sprint. The Software Quality course works with Java, JUnit 5 and Maven/Gradle, so the stack must make unit, parametrised and performance testing easy.

## Options considered

### Backend
| Option | Suitability | Testability | Notes |
|---|---|---|---|
| **Java 21 + Spring Boot 3** | Excellent for REST + layered/domain logic | JUnit 5, Mockito, `@WebMvcTest`, Testcontainers | Matches course tooling |
| Node.js + NestJS (TS) | Good | Jest/Vitest | Diverges from course tooling |
| Python + FastAPI | Good | pytest | Weaker fit with Maven/JUnit topics |

### Frontend
| Option | Pros | Cons |
|---|---|---|
| **React + Vite + TypeScript** | Largest ecosystem, fast dev server, typed API calls, Vitest | Needs our own structure conventions |
| Angular | Opinionated, close to Java mindset | Steeper learning curve for short sprints |
| Vue 3 + Vite | Gentle learning curve | Smaller ecosystem |

### Persistence
| Option | Pros | Cons |
|---|---|---|
| **MySQL 8 + Spring Data JPA + Flyway** | Relational domain (enrolments, grades, competencies); versioned schema | Needs Docker locally |
| PostgreSQL | Equally suitable (first proposal of this ADR) | Team more familiar with MySQL (Bases de Dados course) |
| H2 / SQLite only | Zero setup | Differs from a real server DB |

## Decision
**React + Vite + TypeScript** · **Java 21 + Spring Boot 3.5 (REST/JSON)** · **MySQL 8.4 (Docker Compose locally, managed MySQL for the demo)** · **Spring Data JPA + Flyway** · **Maven** (backend), **npm** (frontend) · Tests: **JUnit 5, Mockito, Testcontainers**, **Vitest + Testing Library**.

## Justification
- **Suitability:** relational DB fits the academic domain; Spring Boot gives a clean layered structure for business rules.
- **Maintainability:** strong typing on both sides; Flyway versions every schema change.
- **Testability:** JUnit 5 parametrised tests align with the course; Testcontainers runs integration tests on real MySQL.
- **Integration:** REST/JSON; the Vite dev proxy forwards `/api` to the backend (no CORS in development).
- **Team knowledge:** Java from the course; MySQL known; React has the most learning material.

## Consequences
- Developers need JDK 21, Node 20+ and Docker; Maven runs through the wrapper (`./mvnw`), so no global install is required.
- Two build tools (Maven + npm), documented in the README.
- Integration tests are skipped automatically when Docker is not available.
