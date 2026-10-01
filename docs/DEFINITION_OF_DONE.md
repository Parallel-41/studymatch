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
- [ ] Project builds from a clean clone with the documented command.
- [ ] Follows the [coding conventions](CODING_CONVENTIONS.md) and agreed package structure.
- [ ] No compiler warnings introduced, no commented-out code, no debug prints.
- [ ] No secrets or credentials committed; config via environment / example files.
- [ ] Existing tests pass; new logic has at least basic unit tests where it makes sense.
- [ ] README / setup instructions updated if how to run the project changed.

## Documentation / models
- [ ] Stored in `docs/` in the agreed folder, with editable source + exported image for diagrams.
- [ ] Consistent with the rest of the documentation (names of use cases and domain concepts match).
- [ ] Important decisions recorded as an ADR in `docs/decisions/`.
