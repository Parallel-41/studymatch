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

| Member | Role in Sprint 1 |
|---|---|
| Daniel | _TBD_ |
| Guilherme | _TBD_ |
| Erzhan | _TBD_ |
| Hugo | _TBD_ |
| Diego | _TBD_ |

## Repository structure

```
StudyMatch/
├── frontend/              # Web client (stack: see ADR-0001)
├── backend/               # API + business logic + persistence
├── docs/
│   ├── decisions/         # Architecture Decision Records (ADRs)
│   ├── use-cases/         # Use-case model
│   ├── domain/            # Domain model (UML)
│   ├── architecture/      # Architecture diagram + explanation
│   ├── CODING_CONVENTIONS.md
│   └── DEFINITION_OF_DONE.md
├── scripts/               # Repo automation (labels, milestone, ...)
├── .github/               # Issue / PR templates
└── CONTRIBUTING.md        # Team workflow (branches, PRs, reviews)
```

## Getting started

> Setup instructions will be completed once the technology stack is decided (see [ADR-0001](docs/decisions/ADR-0001-technology-stack.md)).

### Prerequisites
- Git
- _TBD — e.g. JDK 21, Node.js 20+, Docker_

### Run the backend
```bash
# TBD
```

### Run the frontend
```bash
# TBD
```

## How we work

Every change follows **Issue → Branch → Implementation → Pull Request → Review → Merge**.
Read [CONTRIBUTING.md](CONTRIBUTING.md) before opening your first branch.

## Documentation

- [Contributing / workflow](CONTRIBUTING.md)
- [Definition of Done](docs/DEFINITION_OF_DONE.md)
- [Coding conventions](docs/CODING_CONVENTIONS.md)
- [Architecture decisions](docs/decisions/)
- [Use-case model](docs/use-cases/)
- [Domain model](docs/domain/)
- [Architecture](docs/architecture/)

## Sprint status

| Sprint | Focus | Milestone |
|---|---|---|
| 1 | Project foundation — workflow, use cases, domain, architecture, full-stack skeleton | `Sprint 1` |
