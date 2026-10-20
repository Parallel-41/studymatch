# Sprint 2 Review — demo script

Closes #64 · Sprint 2 · Team Parallel 41

The demo shows four scenarios in about 12 minutes. Each one has a presenter, the data it uses and the exact requests to run, so anyone can repeat it on a clean clone.

## What is in `main` (state on 2026-10-20, 22:45)

| Feature | Issue / PR | In `main`? | Used in |
|---|---|---|---|
| Student API (create, update, list, search) | #37 / #86, #38 / #92 | #86 yes, #92 open | A |
| Academic history + best-grade rule | #41, #42 / #74, #43 / #90 | yes | A |
| Competency catalogue + weights | #46 / #78, #47 / #82 | yes | A, C |
| LEI course units + demo history (V7) | #47 / #84 | **open** | A, B |
| Profile calculator + cold start policy | #48 / #83, #53 / #76 | yes | A, B, C |
| `GET /api/students/{id}/profile` | #49 / #89 | yes | A, B |
| Explanation endpoint | #56 / #93 | **open** | C |
| Self-assessment API | #52 / #75 | **open** | B |
| Profile page (radar) | #50 / #81 | yes (mock data) | A |
| Explanation panel, onboarding form | #57 / #80, #54 / #85 | **open** | C, B |
| CI + JaCoCo | #59 / #68 | yes | D |

Rows marked **open** have a fallback in the scenario below. Update this table on the day of the review.

## Before the review (rehearsal)

Run once on a **clean clone**, the day before:

```bash
git clone https://github.com/Parallel-41/studymatch.git studymatch-demo
cd studymatch-demo
docker compose down -v && docker compose up -d
cd backend && ./mvnw clean verify && ./mvnw spring-boot:run
```

In a second terminal, start the frontend (`cd frontend && npm ci && npm run dev`) and check that the API answers:

```bash
curl -s http://localhost:8080/api/health
curl -s http://localhost:8080/api/students | jq '.[] | {id, number, currentYear}'
```

Write down the ids of `DEMO-A-3` (3rd year) and `DEMO-B-1` (1st year). Below they are `$A` and `$B`:

```bash
A=$(curl -s http://localhost:8080/api/students | jq '.[] | select(.number=="DEMO-A-3") | .id')
B=$(curl -s http://localhost:8080/api/students | jq '.[] | select(.number=="DEMO-B-1") | .id')
```

**If #84 (V7) is not merged**, the LEI course units do not exist yet. Create the two used in the demo before starting:

```bash
docker compose exec mysql mysql -ustudymatch -pchange-me studymatch -e "
INSERT INTO course_unit (code, name, curricular_year, semester, ects, degree_program_id)
SELECT 'PROG1', 'Programação I', 1, 1, 6, id FROM degree_program WHERE code = 'LEI'
UNION ALL SELECT 'DB1', 'Bases de Dados I', 2, 1, 6, id FROM degree_program WHERE code = 'LEI';"
```

V6 already has the weights `PROG1 → PROG_OO 0.6, PROG_ALG 0.4` and `DB1 → DB_SQL 0.8, DB_MODEL 0.2`.

## Scenario A — Student with history (Guilherme + Daniel, 4 min)

**Goal:** the profile is computed from the history and changes as soon as a grade changes (ADR-0003, A4).

1. **Guilherme** shows the student and registers the history live:
   ```bash
   curl -s http://localhost:8080/api/students/$A | jq
   curl -s -X POST http://localhost:8080/api/students/$A/attempts -H 'Content-Type: application/json' \
     -d '{"courseUnitCode":"PROG1","academicYear":"2024/25","season":"NORMAL","grade":12}'
   curl -s -X POST http://localhost:8080/api/students/$A/attempts -H 'Content-Type: application/json' \
     -d '{"courseUnitCode":"DB1","academicYear":"2024/25","season":"NORMAL","grade":14}'
   curl -s http://localhost:8080/api/students/$A/history | jq
   ```
   Point out: grouped by academic year and semester, `bestGrade` per course unit.
2. **Daniel** shows the profile:
   ```bash
   curl -s http://localhost:8080/api/students/$A/profile | jq '.competencies[] | {code, level, confidence}'
   ```
   Point out: `PROG_OO` and `DB_SQL` have levels; competencies without evidence show `level: null` ("no data", never 0).
3. **Change one grade live** — an improvement exam on PROG1:
   ```bash
   curl -s -X POST http://localhost:8080/api/students/$A/attempts -H 'Content-Type: application/json' \
     -d '{"courseUnitCode":"PROG1","academicYear":"2024/25","season":"MELHORIA","grade":18}'
   curl -s http://localhost:8080/api/students/$A/profile | jq '.competencies[] | select(.code=="PROG_OO")'
   ```
   Point out: the `PROG_OO` level goes up on the next request — nothing is stored or recalculated in the background.
4. Show the **profile page** (radar by area) in the browser. Until the page is connected to the API (#50), it shows the shared example data.

## Scenario B — Cold start (Diego, 3 min)

**Goal:** a 1st-year student without grades gets a profile from the self-assessment, and evidence takes over as grades arrive (ADR-0003, F4: `α = min(1, weighted ECTS / 12)`, confidence = α).

1. Profile of `DEMO-B-1` without grades or self-assessment: every competency has `level: null`.
   ```bash
   curl -s http://localhost:8080/api/students/$B/profile | jq '.areas'
   ```
2. **Self-assessment** (needs #75):
   ```bash
   curl -s -X PUT http://localhost:8080/api/students/$B/self-assessment -H 'Content-Type: application/json' \
     -d '{"levels":{"PROG":60,"DB":40,"WEB":50,"QUAL":30,"MATH":70,"SOFT":80}}'
   curl -s http://localhost:8080/api/students/$B/profile | jq '.competencies[] | {code, level, confidence}'
   ```
   Point out: levels come from the self-assessment, **confidence 0**.
3. **First grade:**
   ```bash
   curl -s -X POST http://localhost:8080/api/students/$B/attempts -H 'Content-Type: application/json' \
     -d '{"courseUnitCode":"PROG1","academicYear":"2026/27","season":"NORMAL","grade":15}'
   curl -s http://localhost:8080/api/students/$B/profile | jq '.competencies[] | select(.code=="PROG_OO")'
   ```
   Point out: confidence rises (6 ECTS × 0.6 weight = 3.6 → α = 0.3) and the level moves toward the evidence.

**Fallback if #75 is not merged:** skip step 2 and show `ColdStartPolicyTest` (`α = 0`, `α = 1`, ECTS just below / at / above 12) and section F4 of ADR-0003.

## Scenario C — Team feature: explain a level (Erzhan + Daniel, 3 min)

**Goal:** a student can see where each level comes from (#55).

1. **Daniel** calls the explanation for the competency changed in scenario A (needs #93):
   ```bash
   curl -s http://localhost:8080/api/students/$A/profile/competencies/PROG_OO/explanation | jq
   ```
   Point out: one contribution per course unit (`grade`, `weight`, `recency`, `points`, `share`); the points add up to `evidenceLevel`; `selfAssessedShare` is the part of the self-assessment.
2. **Erzhan** shows the explanation panel on the profile page (#80). If it is not merged, show it running on its branch with the example data.

**Fallback if #93 is not merged:** show `ProfileCalculatorTest.contributionPointsAddUpToTheEvidenceLevelAndSharesToOne` and the worked example in ADR-0003, section 3.

## Scenario D — Build and tests (Hugo, 2 min)

**Goal:** the product is built and tested automatically.

1. Run the checks:
   ```bash
   cd backend && ./mvnw clean verify          # unit, controller and Testcontainers tests
   open target/site/jacoco/index.html         # coverage report
   cd ../frontend && npm test && npm run lint && npm run build
   ```
2. Show a pull request on GitHub with the **backend** and **frontend** checks green, and the `main` rule: 1 approval + green CI before merging.
3. Mention the parameterised tests used for the business rules (`@ParameterizedTest` + `@CsvSource`): grade boundaries 10/20, recency, α = 0 / α = 1.

## Closing (Daniel, 30 s)

- Sprint 2 delivered: academic history, competency profile computed on demand, cold-start policy, profile explanation endpoint, CI.
- Moved to Sprint 3 (with justification in each issue): the items still open in the table above.
- Next: connect the pages to the API (#50), self-assessment form (#54), group formation.
