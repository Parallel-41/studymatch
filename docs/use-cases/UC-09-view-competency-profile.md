# UC-09 — View a competency profile with its explanation

**Objective:** Show what the system believes about a student and why, so the student trusts it and can spot wrong or missing data.  
**Primary actor(s):** Student (own profile), Teacher (students of their contexts)  
**Capability area:** Student profile  
**Target sprint:** 2  
**Preconditions:** User is signed in with access to that profile.  
**Postconditions:** —  

## Main success scenario
1. User opens the profile page (`GET /api/students/{id}/profile`).
2. System shows a radar chart with the level of each official area (`PROG`, `DB`, `WEB`, `QUAL`, `MATH`, `SOFT`) and a visual low-confidence marker where `confidence < 0.5`.
3. User inspects the competency list showing each competency's `level`, `confidence`, `evidenceLevel` and `selfAssessedLevel`.
4. User selects a competency to view its explanation (`GET /api/students/{id}/profile/competencies/{code}/explanation`).
5. System displays the breakdown: each contributing course unit (`courseUnitCode`, `grade`, `weight`, `recency`, `share`) and the `selfAssessedShare`.

## Alternative / exceptional flows
- **2a.** Student has no grades and no self-assessment → empty state with a prompt to complete the onboarding self-assessment (UC-10).
- **3a.** Competency or area without data (`level: null`) → explicitly shown as "No data", never converted to `0` (R9).

## Business rules / constraints
- **BR-09-1:** Area level is the mean of its competencies that have data (`level != null`).
- **BR-09-2:** Course-unit `contributions` and `selfAssessedShare` explain the exact provenance of the competency level (ADR-0003).

## Domain concepts revealed
- `StudentProfile`, `AreaLevel`, `CompetencyLevel`, `Contribution`, `CompetencyArea`