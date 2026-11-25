# ADR-0004 — Matching strategy for automatic group formation

- **Status:** Proposed — open for team comments in the PR until **Tue 24 Nov, 12:00**
- **Date:** 2026-11-23
- **Deciders:** Daniel, Guilherme, Erzhan, Hugo, Diego
- **Issue:** #105

## Context
Sprint 3 must generate student groups automatically (brief §3). The brief imposes no algorithm, but every team must decide and justify which student characteristics are used, how they influence matching, what makes a group appropriate and how criteria are combined. It also fixes hard rules (§3.1): every eligible student is placed exactly once, groups have **4 or 5 students whenever mathematically possible**, impossible configurations are handled explicitly, and generations are persisted and can be consulted later.

Five issues code against these rules in parallel: GroupSizePlanner #106, GroupMatcher #107, explanation #108, generation API #110 and the constraints #114/#115, plus the frontend pages #138–#140. This ADR fixes the rules and the API contract before that code is merged.

Three decisions are open for the team (**D1–D3**). Please comment on each one in the PR.

## Input: what the algorithm knows about a student
The input is the Sprint 2 profile (ADR-0003), computed on demand: for each of the 6 areas (`PROG`, `DB`, `WEB`, `QUAL`, `MATH`, `SOFT`) a `level` in 0–100 (or `null`, "no data") and a `confidence` in 0–1.

- **Effective level** used for matching: `e = level × (0.5 + 0.5 × confidence)`. A level backed by grades (confidence 1) counts fully; a self-assessed level (cold start, confidence 0) counts half.
- An area with `level = null` gives **no coverage** for that area. It is never shown or stored as level 0 (rule R9); the student is still placed in a group.
- Nothing else about a student is used (no name, number, age or grades directly).

## D1 — What makes a good group (strategy)
**Options:** (1) random groups; (2) homogeneous groups (similar students together); (3) **complementary groups, balanced between each other**.
**Proposed:** **(3).**

- **Complementarity (inside a group):** for each area, the strongest member covers it. `coverage(g) = mean over the 6 areas of max e(s, area) for s in g`.
- **Balance (between groups):** groups should be equally strong overall. `spread = standard deviation of the groups' mean effective level`.
- **Combined score** (0–100, one decimal): `score = mean coverage(g) − 0.5 × spread` (`− 0.3 × repeatedPairs` only if the team accepts addition A2).
- **Algorithm:**
  1. Sizes from the GroupSizePlanner (D2).
  2. Together blocks of the constraints placed first (D3).
  3. Greedy seeding: students sorted by overall effective level, dealt in a serpentine order (1→n, n→1) so every group gets strong and weaker students.
  4. Local search: swap two students of different groups whenever the score improves and no constraint breaks; stop after a pass with no improvement (at most 50 passes). Only the two groups involved are recomputed.
  5. Ties are broken with a seeded `Random`; the seed is stored with the generation, so **the same data and seed always give the same groups** (testable).

**Why:** project groups work best when every area has someone able to lead it, and no group is clearly weaker than the others; this matches use case UC-14. Weighting by confidence keeps cold-start students (self-assessment only) from being treated as experts. Swaps are easy to explain and to test, unlike an exact solver.

## D2 — Group sizes and impossible configurations
**Rule for n eligible students:** `groups = ⌈n / 5⌉`. If `4 × groups ≤ n`, sizes are as even as possible with 4s and 5s.

| n | Groups | Result |
|---|---|---|
| 0–3 | — | **422** `"At least 4 students are needed to form groups"`, nothing saved |
| 4, 5 | 1 | 4 / 5 |
| 8, 9, 10 | 2 | 4+4 / 5+4 / 5+5 |
| 12 | 3 | 4+4+4 |
| 13 | 3 | 5+4+4 |
| 23 | 5 | 5+5+5+4+4 (the brief's example) |
| **6, 7, 11** | 2, 2, 3 | No 4/5 split exists → see options |

**Options for 6, 7 and 11:** (a) reject with **422**; (b) **generate the closest split (3+3, 4+3, 4+4+3) and return a `warning`** shown on the groups page and stored with the generation.
**Proposed:** **(b).** The brief asks for 4–5 "whenever mathematically possible" and for impossible cases to be handled explicitly; a visible warning handles it explicitly and still gives the lecturer something to work with.

Tests (#106): parameterised `@CsvSource` with the rows above as equivalence classes and boundaries (0, 1, 3, 4, 5, 6, 7, 8, 11, 12, 13, 23, 100).

## D3 — Team feature
**Options:** (1) **teacher constraints: keep together / keep apart** (UC-13, domain model); (2) configurable weights per area.
**Proposed:** **(1).**

- Types: `TOGETHER` (these students in the same group) and `APART` (no two of these students in the same group). Hard rules: the result never breaks them.
- **Rejected when added** (400, #114): a student who is not a participant; the same pair both together and apart; a together set larger than 5.
- **Checked before generating** (422, nothing saved): more students apart than groups; together blocks that cannot fit in the planned sizes.
- The explanation of a member placed by a constraint says so.

**Why:** it is the feature a lecturer most often needs (friends who must be split, or a pair already working together), it reuses the use case and model we already have, and it gives strong unit tests (each rule alone and combined, contradictions, infeasible sets).

## API contract
Errors keep the current `ApiError` (`timestamp`, `status`, `error`, `message`, `path`); the frontend shows `message`. Examples: `docs/api/examples/grouping-context.json` and `docs/api/examples/group-generation.json`.

| Method and path | Body | Answer |
|---|---|---|
| `POST /api/grouping-contexts` | `{ name, description }` | 201 `GroupingContext` |
| `GET /api/grouping-contexts` | — | 200 `GroupingContext[]` |
| `GET /api/grouping-contexts/{id}` | — | 200 `GroupingContext` · 404 |
| `PUT /api/grouping-contexts/{id}/participants` | `{ studentIds: number[] }` | 200 `GroupingContext` · 404 unknown student · 400 duplicate id |
| `GET /api/grouping-contexts/{id}/constraints` | — | 200 `GroupingConstraint[]` |
| `POST /api/grouping-contexts/{id}/constraints` | `{ type: "TOGETHER" or "APART", studentIds }` | 201 `GroupingConstraint` · 400 |
| `DELETE /api/grouping-contexts/{id}/constraints/{constraintId}` | — | 204 |
| `POST /api/grouping-contexts/{id}/generations` | `{ seed? }` (optional) | 201 `GroupGeneration` · 422 |
| `GET /api/grouping-contexts/{id}/generations` | — | 200 history: `{ id, generatedAt, score, groupCount, warning? }[]`, newest first |
| `GET /api/grouping-contexts/{id}/generations/{generationId}` | — | 200 `GroupGeneration` · 404 |

- `GroupingContext` = `{ id, name, description, createdAt, studentIds }`.
- `GroupingConstraint` = `{ id, type, studentIds }`.
- `GroupGeneration` = `{ id, generatedAt, seed, score, warning?, groups: [{ id, number, score, areas: [{ code, name, level }], explanation, members: [{ studentId, number, name, areas: [{ code, name, level, confidence }], explanation }] }] }`.
- These shapes are the ones the frontend PRs #138–#140 already use; `seed` is new and optional for the frontend.
- A context is not edited after a generation; changing participants and generating again creates a new generation, and old ones stay readable (brief §3.1).

## Justification
- **Suitability:** covers every rule of brief §3.1 and gives a reason per group and per member (§4).
- **Testability:** sizes, score, constraints and determinism are pure functions with clear boundaries (D2 table, each constraint alone and combined, same seed → same groups).
- **Maintainability:** constants (`0.5` confidence floor, `0.5` spread weight, 50 passes) live in one class each; the algorithm has no Spring or database code.
- **Integration:** reuses the Sprint 2 profile unchanged and the contract the frontend already implements.

## Consequences
- The best local result, not the global optimum: the lecturer can generate again with another seed and compare.
- A cold-start student is placed but rarely "covers" an area alone; this is intended and is shown in the demo.
- Generating for many students computes every profile: #110 must load them without N+1 queries (checked again with JMeter, #121/#127).
- Use cases UC-12 to UC-15 (#132) and the domain model v3 (#131) are updated to match: fixed 4–5 sizes, no context states, only together/apart constraints.
