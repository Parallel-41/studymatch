# ADR-0001 — Technology stack

- **Status:** Proposed — to be voted by the team
- **Date:** 2026-09-23
- **Deciders:** Daniel, Guilherme, Erzhan, Hugo, Diego
- **Issue:** #<n>

## Context
StudyMatch must become a maintainable, testable full-stack application (frontend, API, business logic, persistence) whose requirements change every sprint. The Software Quality course works with Java, JUnit 5, Maven/Gradle, so the stack should make unit, parametrised and performance testing easy.

## Options considered

### Backend
| Option | Suitability | Testability | Team knowledge | Notes |
|---|---|---|---|---|
| **Java 21 + Spring Boot 3** | Excellent for REST + layered/domain logic | JUnit 5, Mockito, `@SpringBootTest`, Testcontainers, JaCoCo | Java used in the course | Industry standard; heavy but well documented |
| Node.js + Express/NestJS (TS) | Good | Jest/Vitest | Varies | Same language as frontend, but diverges from course tooling |
| Python + FastAPI | Good | pytest | Varies | Fast to start; weaker fit with Maven/Gradle/JUnit topics |

### Frontend
| Option | Pros | Cons |
|---|---|---|
| **React + Vite + TypeScript** | Most known by the team, huge ecosystem, fast dev server, Vitest + Testing Library | Less opinionated → needs our own structure conventions |
| Angular | Opinionated structure, TS by default, built-in DI and testing | Steeper learning curve for 15-day sprints |
| Vue 3 + Vite | Simple, gentle learning curve | Smaller ecosystem than React |

### Persistence
| Option | Pros | Cons |
|---|---|---|
| **PostgreSQL (Docker) + Spring Data JPA + Flyway** | Relational model fits students/courses/enrolments/competencies; migrations versioned; same DB in dev/test/prod | Requires Docker on every machine |
| H2 / SQLite embedded | Zero setup | Differs from a real server DB; hides production issues |
| MongoDB | Flexible profiles | Academic data is highly relational; weaker constraints |

### Build
| Option | Notes |
|---|---|
| **Maven** (backend) + npm (frontend) | Maven covered in class (MeetingPlanner), conventional, predictable |
| Gradle | Also covered (ShoppingCart); more flexible, Kotlin/Groovy DSL |

## Proposed decision
**React + Vite + TypeScript** · **Java 21 + Spring Boot 3 (REST/JSON)** · **PostgreSQL 16 via Docker Compose** · **Spring Data JPA + Flyway** · **Maven** (backend), **npm** (frontend) · Tests: **JUnit 5, Mockito, Testcontainers**, **Vitest** on the frontend.

## Justification
- **Suitability:** relational DB matches the academic domain (enrolments, attempts, grades, competencies); Spring Boot gives a clean layered/domain structure for business rules.
- **Maintainability:** strong typing on both sides (Java + TS); Flyway versioned schema evolves with each sprint's new requirements.
- **Testability:** JUnit 5 + parametrised tests directly aligned with the course; Testcontainers runs integration tests against real PostgreSQL.
- **Integration:** REST/JSON with OpenAPI (springdoc) documentation; Vite dev proxy avoids CORS issues locally.
- **Team knowledge:** Java from the course; React/TS already known by at least part of the team.
- **Tooling:** IntelliJ/VS Code, GitHub Actions for CI, Docker Compose for a one-command DB.

## Consequences
- Every member needs JDK 21, Node 20+ and Docker installed (setup documented in README).
- Two build tools (Maven + npm) — mitigated by a root-level README with run commands.
- Revisit if the team lacks Docker access (fallback: H2 in PostgreSQL compatibility mode for local dev).
