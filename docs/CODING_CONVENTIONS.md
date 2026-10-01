# Coding conventions

> General rules below apply now. Language-specific sections will be finalised after [ADR-0001](decisions/ADR-0001-technology-stack.md).

## General
- **Language:** code, identifiers, comments, commits, issues and docs in **English**.
- Meaningful names; no abbreviations except well-known ones (`id`, `url`, `dto`).
- One responsibility per class/function (SOLID); prefer small functions (< ~30 lines).
- No magic numbers/strings — use named constants or configuration.
- No commented-out code; no `TODO` without an issue number (`// TODO(#42): ...`).
- Handle errors explicitly; never swallow exceptions silently.
- Formatting is automated (formatter config committed to the repo) — don't argue about style in reviews.
- UTF-8, LF line endings, final newline, 4 spaces (Java) / 2 spaces (JS/TS, JSON, YAML).

## Naming
| Element | Convention | Example |
|---|---|---|
| Classes / interfaces / components | PascalCase | `StudentProfile`, `GroupFormationService` |
| Methods / functions / variables | camelCase | `calculateAffinity()` |
| Constants | UPPER_SNAKE_CASE | `MAX_GROUP_SIZE` |
| Packages (Java) | lowercase | `pt.upt.studymatch.profile` |
| Files (frontend) | kebab-case or PascalCase for components | `student-card.tsx` / `StudentCard.tsx` |
| REST endpoints | plural nouns, kebab-case | `GET /api/students/{id}/course-enrolments` |
| DB tables / columns | snake_case | `course_unit`, `created_at` |

## Backend (proposed, if Java/Spring Boot)
- Base package `pt.upt.studymatch`, **package-by-feature**: `student`, `course`, `competency`, `profile`, `grouping`, `common`.
- Inside each feature: `api` (controllers + DTOs), `application` (services/use cases), `domain` (entities, value objects, rules), `infrastructure` (repositories, adapters).
- Business rules live in `domain`/`application` — **never** in controllers or repositories.
- Controllers return DTOs, never JPA entities.
- Tests: JUnit 5, test class `<ClassName>Test`, method names `should<Result>When<Condition>()`.

## Frontend (proposed)
- Folder by feature: `src/features/<feature>/`, shared UI in `src/components/`, API calls centralised in `src/api/`.
- No direct `fetch` calls inside components — use the API client layer.
- Components small and presentational where possible; state/logic in hooks/services.

## API
- JSON, REST, versioned base path `/api`.
- Standard HTTP status codes; errors as `{ "code", "message", "details" }`.
