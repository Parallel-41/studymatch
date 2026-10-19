# UC-01 — Import academic data from CSV

**Objective:** Load students, course units, enrolments, attempts and UC → competency weights in bulk, so the system starts with real-shaped data instead of manual typing.  
**Primary actor(s):** Admin  
**Capability area:** Identity / Academic trajectory  
**Target sprint:** 2  
**Preconditions:** Admin is signed in.  
**Postconditions:** Accepted rows are stored; affected profiles are up to date; an ImportBatch documents the import.  

## Main success scenario
1. Admin opens Data import and chooses the data type (students, course units, enrolments and attempts, UC → competency weights).
2. System shows the expected CSV columns and a sample file.
3. Admin uploads the CSV file.
4. System validates every row (format, ranges, references to existing students/UCs, duplicates) without saving anything yet.
5. System shows a preview: rows accepted, rows rejected and the reason for each rejection.
6. Admin confirms the import.
7. System saves the accepted rows in one transaction, records an `ImportBatch` and recalculates the profiles of the affected students (UC-08).

## Alternative / exceptional flows
* **3a.** File is not CSV, is empty or has missing columns → System rejects the file and lists the missing columns; nothing is saved.
* **5a.** Every row is rejected → Confirm is disabled; Admin fixes the file and restarts at step 3.
* **6a.** Admin cancels → nothing is saved.
* **7a.** A database error happens while saving → transaction is rolled back, no partial import, error shown.

## Business rules / constraints
* **BR-01-1:** Import is all-or-nothing for the accepted rows (single transaction).
* **BR-01-2:** Existing records are matched by natural key (student number, UC code); a matching row updates the record instead of duplicating it.
* **BR-01-3:** Grades must be integers 0–20; years and semesters must exist in the curricular plan (R1–R3).
* **BR-01-4:** Only fictitious data is used in demos and tests (ADR-0003 §5).

## Domain concepts revealed
* `ImportBatch` — one import with counts and errors
* `Student`, `CourseUnit`, `Enrolment`, `Attempt`, `CourseUnitCompetency` — imported records