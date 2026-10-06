# Coding conventions

> **Status: agreed for Sprint 1** (issue #3). Stack as in [ADR-0001](decisions/ADR-0001-technology-stack.md): Java 21 + Spring Boot 3.5 (Maven), React + Vite + TypeScript, MySQL 8.4 + Flyway.

## General
- **Language:** code, identifiers, comments, commits, issues and docs in **English**.
- Meaningful names; no abbreviations except well-known ones (`id`, `url`, `dto`).
- One responsibility per class/function (SOLID); prefer small functions (< ~30 lines).
- No magic numbers/strings — use named constants or configuration.
- No commented-out code; no `TODO` without an issue number (`// TODO(#42): ...`).
- Handle errors explicitly; never swallow exceptions silently.
- Formatting is automated — don't argue about style in reviews:
  - [`.editorconfig`](../.editorconfig) at the repo root (encoding, line endings, indentation) — IntelliJ and VS Code read it automatically;
  - backend: IntelliJ default Java style (*Reformat Code* before committing, *Optimize imports* on);
  - frontend: **oxlint** (`npm run lint`, config in `frontend/.oxlintrc.json`) and the TypeScript compiler in strict mode (`npm run build`).
- UTF-8, LF line endings, final newline, 4 spaces (Java) / 2 spaces (JS/TS, JSON, YAML).

## Naming
| Element | Convention | Example |
|---|---|---|
| Classes / interfaces / components | PascalCase | `StudentProfile`, `GroupFormationService` |
| Methods / functions / variables | camelCase | `calculateAffinity()` |
| Constants | UPPER_SNAKE_CASE | `MAX_GROUP_SIZE` |
| Packages (Java) | lowercase | `pt.upt.studymatch.profile` |
| Files (frontend) | PascalCase for components, camelCase for everything else | `StudentList.tsx`, `client.ts` |
| Tests | `<ClassName>Test.java` / `<Component>.test.tsx` next to the code | `StudentTest.java`, `StudentList.test.tsx` |
| Flyway migrations | `V<n>__<description>.sql`, never edited after merge | `V3__create_course_unit.sql` |
| REST endpoints | plural nouns, kebab-case | `GET /api/students/{id}/course-enrolments` |
| DB tables / columns | snake_case | `course_unit`, `created_at` |

## Backend (Java 21 + Spring Boot)
- Base package `pt.upt.studymatch`, **package-by-feature**: `student`, `course`, `competency`, `profile`, `grouping`, `common`.
- Inside each feature: `api` (controllers + DTOs), `application` (services/use cases), `domain` (entities, value objects, rules), `infrastructure` (repositories, adapters).
- Business rules live in `domain`/`application` — **never** in controllers or repositories.
- Controllers return DTOs, never JPA entities.
- Constructor injection only (no `@Autowired` on fields); classes `final` fields where possible. No Lombok.
- DTOs are Java `record`s (`StudentResponse`); validation with Bean Validation (`@NotBlank`, `@Size`…).
- Configuration in `application.yml`; secrets only through environment variables (`.env`, never committed — `.env.example` is).
- Time comes from an injected `Clock`, never `Instant.now()`/`LocalDate.now()` directly (testable).
- Tests: JUnit 5 (`org.junit.jupiter`) + Mockito + AssertJ; test class `<ClassName>Test`, method names `should<Result>When<Condition>()`.
  - Domain rules → parametrised tests (`@ParameterizedTest`, `@CsvSource`/`@ValueSource`) covering equivalence partitions and boundary values.
  - Controllers → `@WebMvcTest`; services → Mockito; database → Testcontainers integration tests (`*IntegrationTest`).

```
backend/src/main/java/pt/upt/studymatch/
├── StudymatchApplication.java
├── common/          # api (ApiError, GlobalExceptionHandler), config (ClockConfig)
└── student/
    ├── api/             # StudentController, StudentResponse
    ├── application/     # StudentService
    ├── domain/          # Student
    └── infrastructure/  # StudentRepository
backend/src/main/resources/
├── application.yml
└── db/migration/    # V1__create_student.sql, V2__seed_sample_students.sql
```

## Frontend (React + Vite + TypeScript)
- Folder by feature: `src/features/<feature>/`, shared UI in `src/components/`, API calls centralised in `src/api/`.
- No direct `fetch` calls inside components — use the API client layer.
- Components small and presentational where possible; state/logic in hooks/services.
- Function components with hooks; TypeScript `strict`, no `any` (use the types in `src/api/types.ts`).
- Every component that loads data handles **loading, empty and error** states (error with `role="alert"`).
- Tests with Vitest + Testing Library: query by role/text as a user would, not by CSS class.

```
frontend/src/
├── api/             # client.ts (getJson, api.*), types.ts
├── components/      # shared UI
├── features/
│   ├── health/      # HealthBadge.tsx (+ .test.tsx)
│   └── students/    # StudentList.tsx, StudentList.test.tsx
├── test/setup.ts
├── App.tsx
└── main.tsx
```

## API
- JSON, REST, versioned base path `/api`.
- Standard HTTP status codes; errors as `{ "code", "message", "details" }`.
