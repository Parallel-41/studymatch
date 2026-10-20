# ADR-0003 — Competency profile and cold start

- **Status:** Proposed — open for team comments in the PR until the end of day 2
- **Date:** 2026-10-17
- **Deciders:** Daniel, Guilherme, Erzhan, Hugo, Diego
- **Issue:** #60

## Context
Sprint 2 introduces the core of StudyMatch: a competency profile (level 0–100 per competency) built from a student's academic history, and a cold-start strategy for students with little or no history, based on a self-assessment. Several people code against these rules in parallel (ProfileCalculator #48, ColdStartPolicy #53, ProfileService #49, explanation #56, profile page #50/#57), so the formula must be fixed in writing before implementation.

The Sprint 2 plan left five open questions: A4 (stored vs computed profile) and F1–F4 (formula). This ADR answers them. Each decision links the PR that implements it.

## 1. Competency model

- The **catalogue is global** (shared by all degree programmes): 6 areas, each with its competencies.

  | Area | Competencies |
  |---|---|
  | `PROG` Programming | `OOP`, `JAVA`, `DSA` |
  | `DB` Databases | `SQL`, `MODEL` |
  | `WEB` Web | `FRONT`, `BACK` |
  | `QUAL` Software quality | `TEST`, `VCS`, `ARCH` |
  | `MATH` Mathematics | `DMATH`, `STAT` |
  | `SOFT` Soft skills | `TEAM`, `COMM`, `PM` |

- **Course units and their weights belong to a degree programme.** A course unit contributes to one or more competencies with a weight in ]0, 1] (`CourseUnitWeight`). Sprint 2 loads LEI data only.
- The profile shows **only the areas with at least one weight in the student's programme**, so a future programme without e.g. `WEB` units does not show an empty `WEB` area.
- Self-assessment is **per area** (A6): about 6 questions for a new student. Every competency uses the self-assessed level of its area.

Implemented in: #46 (catalogue, V4), #47 (weights + demo data, V6) — _PR links added when merged_.

## 2. Decisions

### A4 — Stored vs computed profile
**Options:** (1) store `StudentProfile`/`CompetencyLevel` in tables and update them; (2) compute the profile on every request.
**Decision:** **(2) computed on demand.** `StudentProfile`, `CompetencyLevel` and `AreaLevel` are value objects (records), with no tables.
**Why:** a stored profile can show stale values when a grade is added or corrected live (demo scenario); the dataset is small (one student, ~30 course units), so computing is cheap; pure functions are easy to test.
**Revisit when:** profile computation becomes measurably slow (performance tests, Sprint 3) — then cache, never store as source of truth.
_Implemented in: #49 — PR link._

### F1 — What is a pass with 10 worth?
**Options:** (1) `(g − 10) / 10 × 100` → 10 gives 0, the same as "knows nothing"; (2) linear 10 → 50, 20 → 100.
**Decision:** **(2)** `score(g) = 50 + (g − 10) × 5`, applied only to **passing** grades (best passing grade per course unit, #42).
**Why:** a student who passed has shown at least basic competence and must not look empty; the scale stays linear and easy to explain.

| Grade | 10 | 12 | 14 | 15 | 16 | 18 | 20 |
|---|---|---|---|---|---|---|---|
| Score | 50 | 60 | 70 | 75 | 80 | 90 | 100 |

_Implemented in: #48 — PR link._

### F2 — Failed attempts
**Options:** (1) ignore them; (2) use them to lower the level; (3) use them to lower confidence.
**Decision:** **(1) for Sprint 2.** Only the best passing grade of each course unit counts (`EvidenceGrade` has no attempt count). A course unit with no passing grade gives no evidence.
**Why:** keeps the `EvidenceGrade` contract (#48) and the explanation simple; lowering the level would punish students who failed and then passed well.
**Revisit in Sprint 3:** option (3), adding `failedAttempts` to `EvidenceGrade` and reducing confidence.
_Implemented in: #42, #48 — PR links._

### F3 — Recency
**Problem:** a recency factor inside a weighted average cancels out when all grades are equally old, so old-only evidence would look as reliable as recent evidence.
**Decision:** recency is used **twice**:
1. in the **level**, to make recent units count more than old ones;
2. in the **amount of evidence** that drives α (F4), so that old-only evidence lowers confidence.

For a competency `c` and each passed course unit `i` with a weight for `c`:

```
r_i  = 0.85 ^ semestersAgo_i          (recency, 1 for the current semester)
w_i  = ects_i × weight_i,c × r_i      (how much unit i counts for c)
evidenceLevel_c = Σ (w_i × score(grade_i)) / Σ w_i
evidenceEcts_c  = Σ w_i               (recency-discounted, weighted ECTS)
```

No passed unit for `c` → `evidenceLevel_c` is empty (never 0).
_Implemented in: #48 — PR link._

### F4 — When evidence takes over from cold start (α)
**Options:** (1) `α = min(1, ECTS / 30)` with raw ECTS of all units — one semester is enough for every competency, even the ones never taught; (2) α per competency, from the weighted, recency-discounted ECTS of F3.
**Decision:** **(2)** with a threshold of **12 weighted ECTS** (two fully relevant 6-ECTS units):

```
α_c          = min(1, evidenceEcts_c / 12)
level_c      = α_c × evidenceLevel_c + (1 − α_c) × selfAssessed_area(c)
confidence_c = α_c
```

| Evidence | Self-assessment | Result |
|---|---|---|
| none | none | `level = null`, `confidence = 0` ("no data") |
| none | yes | `level = selfAssessed`, `confidence = 0` |
| yes | none | `level = evidenceLevel`, `confidence = α` |
| yes | yes | blend above, `confidence = α` |

**Why:** a 1st-year student starts on self-assessment and moves to evidence competency by competency, as units that teach it are passed; old-only evidence counts less (F3). The threshold is a constant in `ColdStartPolicy`, so it can be tuned after the demo.
_Implemented in: #53 — PR link._

### Areas
`areaLevel = mean of the levels of its competencies that have data`; `areaConfidence = mean of their confidences`. An area with no competency data has `level = null`.

## 3. Worked example (for `ProfileCalculatorTest`)
Competency `OOP` (area `PROG`), self-assessment `PROG = 50`:

| Unit | ECTS | Best grade | Weight for OOP | Semesters ago | r | w | score |
|---|---|---|---|---|---|---|---|
| A | 6 | 12 | 1.0 | 2 | 0.7225 | 4.335 | 60 |
| B | 6 | 15 | 0.5 | 0 | 1.0 | 3.000 | 75 |

```
evidenceLevel = (4.335 × 60 + 3 × 75) / 7.335 = 66.1
evidenceEcts  = 7.335   →   α = 7.335 / 12 = 0.611
level         = 0.611 × 66.1 + 0.389 × 50 = 59.9     confidence = 0.61
```

Explanation (#56): unit A contributes `4.335 × 60 / 7.335 = 35.5` and unit B `30.7`; the contributions add up to the evidence level (66.1), and the self-assessment share is `1 − α = 0.39`.

## Justification
- **Suitability:** answers the product questions of the Sprint 2 plan (passing students never look empty, history replaces self-assessment gradually).
- **Testability:** every rule is a pure function with clear boundaries (grade 10/20, α = 0/1, ECTS just below / at / above 12, empty history).
- **Maintainability:** constants (`0.85`, `12`, the F1 scale) live in one place each (`ProfileCalculator`, `ColdStartPolicy`).
- **Integration:** matches the shared contracts (#48) and `profile.json` / `explanation.json`.

## Consequences
- The API returns `level` 0–100 with 1 decimal and `confidence` 0–1; `level: null` means "no data" and the frontend never shows it as 0.
- Changing a grade changes the profile on the next request (no recomputation job).
- A competency taught by a single unit can never reach full confidence unless the unit has ≥ 12 weighted ECTS; this is intended and is shown in the demo (Scenario B).
- Failed attempts carry no information yet (F2) — to revisit in Sprint 3.
