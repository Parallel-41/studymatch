# Definition of Done — Sprint 1

An issue is **Done** only when every applicable item is checked.

## All work
- [ ] Linked to an Issue with milestone `Sprint 1`, labels and a Project board card.
- [ ] Developed on a branch following the naming convention (never on `main`).
- [ ] Merged through a Pull Request with `Closes #<issue>`.
- [ ] Reviewed and approved by **another** team member; all review threads resolved.
- [ ] Acceptance criteria of the issue satisfied.
- [ ] Branch deleted after merge; card moved to *Done*.

## Code
- [ ] Project builds from a clean clone with the documented command:
  backend `./mvnw verify`, frontend `npm test && npm run lint && npm run build`.
- [ ] Follows the [coding conventions](CODING_CONVENTIONS.md) and agreed package structure.
- [ ] No compiler warnings introduced, no commented-out code, no debug prints.
- [ ] No secrets or credentials committed; config via environment / example files.
- [ ] Existing tests pass; every new domain rule has a unit test (parametrised with boundary values when it has ranges), every endpoint a `@WebMvcTest`, every data-loading component a Vitest test.
- [ ] README / setup instructions updated if how to run the project changed.

## Documentation / models
- [ ] Stored in `docs/` in the agreed folder, with editable source + exported image for diagrams.
- [ ] Consistent with the rest of the documentation (names of use cases and domain concepts match).
- [ ] Important decisions recorded as an ADR in `docs/decisions/`.

## Sprint 1 — done for the sprint
- [ ] All Sprint 1 issues closed or explicitly moved to the next milestone with a comment.
- [ ] `main` runs end to end: `docker compose up -d mysql`, backend, frontend showing the sample students.
- [ ] Every member has at least one merged PR and one review given.
- [ ] At least one PR has a real review discussion (comments answered with changes or reasons).

## Agreement
This DoD and the [coding conventions](CODING_CONVENTIONS.md) were agreed in the PR for issue #3: every member left a comment there.
