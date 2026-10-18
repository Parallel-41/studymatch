# UC-04 — Record or correct an assessment attempt

**Objective:** Register a grade (or correct a wrong one) so the trajectory and the profile reflect reality.  
**Primary actor(s):** Admin  
**Capability area:** Academic trajectory  
**Target sprint:** 2  
**Preconditions:** Student and course unit exist.  
**Postconditions:** Trajectory and profile are consistent with the new attempt.  

## Main success scenario
1. Admin opens a student's trajectory and selects an enrolment (or creates one for a UC and academic year).
2. Admin adds an attempt: season, date and grade (or edits an existing attempt).
3. System validates the attempt and saves it.
4. System updates the enrolment status (R3) and recalculates the student's profile (UC-08).
5. System shows the updated trajectory.

## Alternative / exceptional flows
* **3a.** Grade not an integer 0–20 → rejected.
* **3b.** Improvement attempt on an enrolment that is not passed → rejected (R4).
* **3c.** Enrolment for the same UC and year already exists → System opens the existing one instead (R2).
* **3d.** Date in the future → rejected.

## Business rules / constraints
* **BR-04-1:** Grade 0–20; passed when ≥ 10 (R3).
* **BR-04-2:** An improvement attempt only replaces the grade when higher (R4).
* **BR-04-3:** The evidence grade of a UC is the best passed grade over all enrolments (R5).

## Domain concepts revealed
* `Enrolment`, `Attempt`, `Grade` (value object), `AssessmentSeason`, `EnrolmentStatus`