# UC-07 — Map a course unit to competencies

**Objective:** State how much each UC develops each competency — the bridge between grades and the profile.  
**Primary actor(s):** Admin, Teacher (for the UCs they teach)  
**Capability area:** Competencies  
**Target sprint:** 2  
**Preconditions:** UC and competencies exist in the catalogue.  
**Postconditions:** Mapping saved; subsequent profile requests reflect the new weights.  

## Main success scenario
1. User opens a course unit and chooses Competencies.
2. System shows the current competency weights (`CourseUnitWeight`).
3. User adds, changes or removes competency weights using official competency codes (e.g., `PROG_OO`, `DB_SQL`, `QUAL_TEST`).
4. System validates and saves the mapping in `course_unit_competency`.
5. System recalculates the profiles of every student with evidence in that UC on demand (UC-08).

## Alternative / exceptional flows
- **4a.** Weight outside `]0, 1]` or unknown competency/UC code → rejected.
- **4b.** Removing the last competency of a UC → rejected (R6).
- **1a.** Teacher opens a UC they do not teach → read-only view.

## Business rules / constraints
- **BR-07-1:** Weight must be in `]0, 1]` (weights do not need to sum to 1); every UC contributes to at least one official competency (R6).
- **BR-07-2:** Changing a weight immediately affects profile calculations on the next request (R8).

## Domain concepts revealed
- `CourseUnitCompetency`, `CourseUnitWeight` (value object)