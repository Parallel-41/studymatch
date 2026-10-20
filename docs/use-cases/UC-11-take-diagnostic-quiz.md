# UC-11 — Take a diagnostic mini-quiz

**Objective:** Add observed evidence to the self-reported one, so cold start does not rely only on self-perception.  
**Primary actor(s):** Student  
**Capability area:** Cold start  
**Target sprint:** 3  
**Preconditions:** Student is signed in.  
**Postconditions:** Quiz score stored; profile recalculated.  

## Main success scenario
1. Student chooses an official competency area (`PROG`, `DB`, `WEB`, `QUAL`, `MATH`, `SOFT`) on the cold-start page and starts its quiz.
2. System shows the questions (multiple choice) one at a time.
3. Student answers all questions and submits.
4. System scores the attempt (0–100) and stores a `QuizAttempt`.
5. System recalculates the profile (UC-08) and shows the score.

## Alternative / exceptional flows
- **1a.** The area has no active quiz → only the self-assessment (UC-10) is available.
- **3a.** Student leaves before submitting → nothing is stored.
- **4a.** Student retakes the quiz → the best attempt is used (R10).

## Business rules / constraints
- **BR-11-1:** In Sprint 2 cold start uses `SelfAssessment` only; from Sprint 3, when a quiz exists for the area, cold-start level = `0.4 × self-assessment + 0.6 × quiz` (ADR-0003 §3).
- **BR-11-2:** A quiz has at least 3 questions and targets a valid catalogue area code.

## Domain concepts revealed
- `Quiz`, `QuizQuestion`, `QuizAttempt`, `CompetencyArea`