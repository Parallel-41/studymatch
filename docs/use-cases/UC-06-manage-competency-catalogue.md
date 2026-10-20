# UC-06 — Manage the competency catalogue

**Objective:** Define which competencies and areas exist, so the profile speaks the language of the degree.  
**Primary actor(s):** Admin  
**Capability area:** Competencies  
**Target sprint:** 2  
**Preconditions:** Admin is signed in.  
**Postconditions:** Catalogue updated.  

## Main success scenario
1. Admin opens Competencies.
2. System lists the areas and their competencies.
3. Admin creates, renames or describes an area or a competency.
4. System validates and saves.

## Alternative / exceptional flows
* **4a.** Code already exists → rejected.
* **3a.** Admin tries to delete a competency used by UC weights or profiles → System blocks it and shows where it is used; it can be renamed but not deleted.

## Business rules / constraints
* **BR-06-1:** Every competency belongs to exactly one area; codes are unique.
* **BR-06-2:** The catalogue starts with 6 areas (Programming, Databases, Web, Software Quality, Algorithms & Maths, Soft skills) — ADR-0003 §1.

## Domain concepts revealed
* `CompetencyArea`, `Competency`