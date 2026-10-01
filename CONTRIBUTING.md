# Contributing — Team Parallel 41 workflow

All relevant work is traceable: **Issue → Branch → Implementation → Pull Request → Review → Merge**.
Nobody commits directly to `main`.

## 1. Issues

- Every piece of work (feature, doc, bug, chore) starts as a **GitHub Issue** using one of the templates.
- Each issue has: a clear title, acceptance criteria, **at least one `type:` label and one `area:` label**, the current **milestone** (e.g. `Sprint 1`) and is added to the **Project board**.
- **Assignment:** a member assigns themselves when moving the card to *In Progress*. One assignee per issue (pairing is fine — mention the partner in the description).
- Keep issues small (≤ 2 days of work). If it grows, split it.

### Board columns
`Backlog` → `To Do (sprint)` → `In Progress` → `In Review` → `Done`

## 2. Branches

Model: **GitHub Flow** — `main` is always buildable; short-lived branches off `main`.

**Naming:** `<type>/<issue-number>-<short-kebab-description>`

| Type | Use for | Example |
|---|---|---|
| `feature/` | new functionality | `feature/14-student-read-endpoint` |
| `fix/` | bug fixes | `fix/27-null-profile-crash` |
| `docs/` | documentation, models, diagrams | `docs/5-domain-model` |
| `test/` | tests only | `test/31-profile-service-tests` |
| `refactor/` | code restructuring, no behaviour change | `refactor/40-extract-matching-service` |
| `chore/` | build, config, CI, dependencies | `chore/3-setup-maven` |

```bash
git switch main && git pull
git switch -c feature/14-student-read-endpoint
```

## 3. Commits

[Conventional Commits](https://www.conventionalcommits.org/) in English, imperative mood, referencing the issue:

```
feat(backend): add GET /api/students/{id} endpoint (#14)
docs(domain): add initial domain model (#5)
fix(frontend): handle empty profile response (#27)
```
Types: `feat`, `fix`, `docs`, `test`, `refactor`, `chore`, `style`, `build`, `ci`.
Small, focused commits. Never commit secrets, `.env` files, build output or IDE files.

## 4. Pull Requests

- Open a PR as soon as the work is reviewable (use **Draft** PRs for early feedback).
- Fill in the PR template. The description **must** include `Closes #<issue>` so the issue closes on merge.
- Keep PRs small (ideally < 400 changed lines).
- Rebase/merge `main` into your branch and resolve conflicts **before** requesting review.
- Link the PR to the milestone and Project board (move card to *In Review*).

## 5. Reviews

- **Minimum 1 approval from another member.** Authors never approve their own PR.
- Reviewer is chosen by rotation (see table) or requested explicitly for domain expertise. Target: first review within **24 h**.
- Reviewers check the [Definition of Done](docs/DEFINITION_OF_DONE.md) and [coding conventions](docs/CODING_CONVENTIONS.md), and leave **concrete comments** (what, why, suggestion). "LGTM" alone is not a review for non-trivial changes.
- Use GitHub review states: *Comment*, *Request changes*, *Approve*. The author resolves each thread (fix or reply) — the reviewer marks it resolved.

**Review rotation (author → default reviewer):**

| Author | Reviewer |
|---|---|
| Daniel | Guilherme |
| Guilherme | Erzhan |
| Erzhan | Hugo |
| Hugo | Diego |
| Diego | Daniel |

## 6. Merge & branch closure

- Merge only when: ≥ 1 approval, all conversations resolved, build passes, DoD met.
- Strategy: **Squash and merge** (one clean commit per issue on `main`, title in Conventional Commit format).
- The **author** merges after approval.
- **Delete the branch immediately after merge** (enable *Automatically delete head branches* in repo settings) and delete it locally:
  ```bash
  git switch main && git pull && git branch -d feature/14-student-read-endpoint
  ```

## 7. Repository rules (settings)

- `main` protected: PR required, 1 approval, dismiss stale approvals, no force-push, no deletion.
- Auto-delete head branches: **on**.
- Allowed merge method: **squash** only.
