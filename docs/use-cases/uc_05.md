# UC-05 — View my academic trajectory

**Objective:** Let a student see their history by year and semester, including repetitions and improvements.  
**Primary actor(s):** Student (also Teacher for students of their contexts)  
**Capability area:** Academic trajectory  
**Target sprint:** 2  
**Preconditions:** Student is signed in.  
**Postconditions:** —  

## Main success scenario
1. Student opens My trajectory.
2. System lists the enrolments grouped by academic year and semester, with UC, ECTS, attempts, final grade and status.
3. System shows totals: ECTS completed, weighted average of passed UCs, UCs in progress.

## Alternative / exceptional flows
* **2a.** Student has no enrolments → empty state explaining that the profile is based on the cold-start questionnaire until grades exist (link to UC-10).
* **2b.** A UC was repeated → both enrolments are shown, the failed one marked as history.

## Business rules / constraints
* **BR-05-1:** Weighted average uses ECTS as weights and only passed UCs (evidence grade, R5).
* **BR-05-2:** A student sees only their own trajectory (BR-03-2).

## Domain concepts revealed
* `Enrolment`, `Attempt`, `CourseUnit`, `AcademicYear`