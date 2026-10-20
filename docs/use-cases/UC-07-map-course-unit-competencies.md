# UC-07 — Map a course unit to competencies

**Objective:** State how much each UC develops each competency — the bridge between grades and the profile.  
**Primary actor(s):** Admin, Teacher (for the UCs they teach)  
**Capability area:** Competencies  
**Target sprint:** 2  
**Preconditions:** UC and competencies exist.  
**Postconditions:** Mapping saved; affected profiles recalculated.  

## Main success scenario
1. User opens a course unit and chooses Competencies.
2. System shows the current weights.
3. User adds, changes or removes competency weights.
4. System validates and saves the mapping.
5. System recalculates the profiles of every student with evidence in that UC (UC-08).

## Alternative / exceptional flows
* **4a.** Weight outside ]0, 1] → rejected.
* **4b.** Removing the last competency of a UC → rejected (R6).
* **1a.** Teacher opens a UC they do not teach → read-only view.

## Business rules / constraints
* **BR-07-1:** Weight in ]0, 1]; every UC contributes to at least one competency (R6).
* **BR-07-2:** Changing a weight triggers profile recalculation (R8).

## Domain concepts revealed
* `CourseUnitCompetency`, `Weight` (value object)