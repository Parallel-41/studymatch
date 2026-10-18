# UC-09 — View a competency profile with its explanation

**Objective:** Show what the system believes about a student and why, so the student trusts it and can correct wrong data.  
**Primary actor(s):** Student (own profile), Teacher (students of their contexts)  
**Capability area:** Student profile  
**Target sprint:** 2  
**Preconditions:** User is signed in with access to that profile.  
**Postconditions:** —  

## Main success scenario
1. User opens the profile.
2. System shows a radar chart with the level of each area and a low confidence marker where confidence < 0.5.
3. User selects an area.
4. System lists its competencies with level, confidence and source (grades, cold start or both).
5. User selects a competency.
6. System shows the contributions: each UC, its grade, weight, recency and share of the level, plus the cold-start part.

## Alternative / exceptional flows
* **2a.** Profile empty → empty state with a link to the self-assessment (UC-10).
* **4a.** Competency without data → shown as no data, never as 0 (R9).

## Business rules / constraints
* **BR-09-1:** Area level = mean of its competencies with data.
* **BR-09-2:** The shares shown add up to the evidence level (traceability of the calculation).

## Domain concepts revealed
* `StudentProfile`, `CompetencyLevel`, `EvidenceContribution`, `CompetencyArea`