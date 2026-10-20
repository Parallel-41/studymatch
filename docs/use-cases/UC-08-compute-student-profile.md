# UC-08 — Compute or refresh the student profile

**Objective:** Turn academic evidence grades and cold-start answers into a competency level and confidence value for every competency and area.  
**Primary actor(s):** System (computed on demand on `GET /api/students/{id}/profile` and triggered by UC-01, UC-04, UC-07, UC-10, UC-11)  
**Capability area:** Student profile  
**Target sprint:** 2  
**Preconditions:** Student exists.  
**Postconditions:** Computed profile reflects all current academic evidence and self-assessments.  

## Main success scenario
1. System receives a profile request for a student.
2. For each official competency (`PROG_OO`, `DB_SQL`, etc.), `ProfileCalculator` collects the student's `EvidenceGrade`s (best passing grade ≥ 10 per passed UC, ECTS and `semestersAgo`) and the `CourseUnitWeight`s (ADR-0003 §2).
3. System computes the academic `evidenceLevel` and total `evidenceEcts` for the competency.
4. System retrieves the self-assessed level (`SelfAssessment`) for the competency's area (`PROG`, `DB`, `WEB`, `QUAL`, `MATH`, `SOFT`) (ADR-0003 §3).
5. `ColdStartPolicy` blends both using α = min(1, evidenceEcts / 30): `final = α × evidenceLevel + (1 − α) × selfAssessedLevel`, with `confidence = α`.
6. System aggregates competency levels into `AreaLevel`s and returns the `StudentProfile`.

## Alternative / exceptional flows
- **2a.** No academic evidence and no cold-start data for a competency → `level` is `null` ("No data", never `0`) and `confidence` is `0` (R9).
- **2b.** No academic evidence (`evidenceEcts = 0`) but cold-start data exists → `level = selfAssessedLevel`, `confidence = 0` (α = 0, R9).
- **5a.** Academic evidence exists but no cold-start data → `level = evidenceLevel`, `confidence = α`.

## Business rules / constraints
- **BR-08-1:** Normalised grade = `(grade − 10) / 10 × 100`; recency factor = `0.85^semestersAgo` (F1, F3).
- **BR-08-2:** Evidence level = `Σ(normalised × weight × recency) / Σ(weight × recency)`.
- **BR-08-3:** All levels are in `[0, 100]` (or `null` when no data) and confidence is in `[0, 1]` (R7, R9).
- **BR-08-4:** Calculation is pure and deterministic: `StudentProfile`, `AreaLevel` and `CompetencyLevel` are computed on every request from current data.

## Domain concepts revealed
- `StudentProfile`, `AreaLevel`, `CompetencyLevel`, `ProfileCalculator`, `ColdStartPolicy`, `EvidenceGrade`, `CourseUnitWeight`, `Level`, `Confidence`