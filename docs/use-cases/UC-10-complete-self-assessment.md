# UC-10 — Complete the cold-start self-assessment

**Objective:** Let a student with little or no academic history describe their level per competency area, so they can be profiled from day one.  
**Primary actor(s):** Student  
**Capability area:** Cold start  
**Target sprint:** 2  
**Preconditions:** Student exists and is signed in.  
**Postconditions:** Cold-start `SelfAssessment` records stored per area; subsequent profile requests blend the new answers.  

## Main success scenario
1. System detects that the student has areas without academic evidence and invites them to the onboarding self-assessment.
2. System loads the official competency areas (`PROG`, `DB`, `WEB`, `QUAL`, `MATH`, `SOFT`) from the catalogue.
3. Student rates each competency area from `0` to `100` using sliders with anchor descriptions (`25` / `50` / `75` / `100`).
4. Student submits (`PUT /api/students/{id}/self-assessment`).
5. System validates the areas and ranges, stores one `SelfAssessment` per area with the current date (`Clock`), and returns the updated levels so the profile is refreshed (UC-08).

## Alternative / exceptional flows
- **3a.** Student skips an area → that area keeps no cold-start data (`selfAssessedLevel: null`).
- **1a.** Student already completed a self-assessment → the form opens pre-filled (`GET /api/students/{id}/self-assessment`); submitting updates the existing area records and assessment date (R10).
- **5a.** Level outside `[0, 100]` or unknown `areaCode` (not in `competency_area`) → rejected with HTTP 400 (`ApiError`).

## Business rules / constraints
- **BR-10-1:** Only official catalogue area codes (`PROG`, `DB`, `WEB`, `QUAL`, `MATH`, `SOFT`) are accepted.
- **BR-10-2:** Self-assessment is self-reported (`confidence = 0` when alone), and its weight `(1 − α)` fades linearly as academic `evidenceEcts` grows towards 30 ECTS (F4, ADR-0003 §3).
- **BR-10-3:** At most one active self-assessment exists per `(student, areaCode)`; new submissions overwrite the previous level and timestamp (R10).

## Domain concepts revealed
- `SelfAssessment`, `Level`, `CompetencyArea`, `ColdStartPolicy`