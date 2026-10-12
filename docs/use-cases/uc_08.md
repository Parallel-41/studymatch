# UC-08 — Compute or refresh the student profile

**Objective:** Turn grades and cold-start answers into a competency level with a confidence value for every competency.  
**Primary actor(s):** System (triggered by UC-01, UC-04, UC-07, UC-10, UC-11)  
**Capability area:** Student profile  
**Target sprint:** 2  
**Preconditions:** Student exists.  
**Postconditions:** Profile reflects all current evidence.  

## Main success scenario
1. System receives a trigger for one student.
2. For each competency, System collects the evidence: evidence grades of the UCs that contribute to it, with weight and recency (ADR-0003 §2).
3. System computes the evidence level and `evidenceEcts`.
4. System computes the cold-start level of the competency's area (ADR-0003 §3).
5. System combines both with α = min(1, evidenceEcts / 30) into the final level and stores level, parts and confidence.
6. System updates `StudentProfile.updatedAt`.

## Alternative / exceptional flows
* **2a.** No evidence and no cold-start data for a competency → competency is absent from the profile (R9).
* **2b.** No evidence but cold-start data → level = cold-start level, confidence 0 (R9).
* **5a.** Evidence but no cold-start data → level = evidence level, confidence α.

## Business rules / constraints
* **BR-08-1:** Normalised grade = (grade − 10) / 10 × 100; recency factor 0.85ⁿ.
* **BR-08-2:** Evidence level = Σ(normalised × weight × recency) / Σ(weight × recency).
* **BR-08-3:** All levels in [0, 100], confidence in [0, 1] (R7).
* **BR-08-4:** Calculation is deterministic: the same data always gives the same profile.

## Domain concepts revealed
* `StudentProfile`, `CompetencyLevel`, `EvidenceContribution`, `Level`, `Confidence`