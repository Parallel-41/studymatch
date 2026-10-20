# UC-06 — Manage the competency catalogue

**Objective:** Define which competencies and areas exist, so the profile speaks the language of the degree.  
**Primary actor(s):** Admin  
**Capability area:** Competencies  
**Target sprint:** 2  
**Preconditions:** Admin is signed in.  
**Postconditions:** Catalogue updated.  

## Main success scenario
1. Admin opens Competencies (`GET /api/competencies`).
2. System lists the competency areas and their competencies ordered by code.
3. Admin creates, renames or describes an area or a competency.
4. System validates and saves.

## Alternative / exceptional flows
- **4a.** Code already exists → rejected.
- **3a.** Admin tries to delete a competency used by UC weights or profiles → System blocks it and shows where it is used; it can be renamed but not deleted.

## Business rules / constraints
- **BR-06-1:** Every competency belongs to exactly one area; area and competency codes are unique and uppercase.
- **BR-06-2:** The official catalogue starts with 6 areas (`PROG`, `DB`, `WEB`, `QUAL`, `MATH`, `SOFT`) and 15 competencies (`PROG_OO`, `PROG_ALG`, `PROG_ARCH`, `DB_SQL`, `DB_MODEL`, `WEB_FE`, `WEB_BE`, `QUAL_TEST`, `QUAL_CI`, `QUAL_REQ`, `MATH_DISC`, `MATH_STAT`, `SOFT_COMM`, `SOFT_TEAM`, `SOFT_LEAD`) — ADR-0003 §1.

## Domain concepts revealed
- `CompetencyArea`, `Competency`