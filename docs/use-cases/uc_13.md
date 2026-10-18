# UC-13 — Define grouping constraints

**Objective:** Let the teacher impose rules that the formation must always respect.  
**Primary actor(s):** Teacher  
**Capability area:** Grouping context  
**Target sprint:** 4  
**Preconditions:** Context in DRAFT.  
**Postconditions:** Constraints saved.  

## Main success scenario
1. Teacher opens a context in `DRAFT` and chooses Constraints.
2. Teacher adds a constraint: students together, students apart, or required competency (competency, minimum level, minimum members per group).
3. System checks that the constraint is consistent with the context and the other constraints.
4. System saves it and shows the list with a readable description of each rule.

## Alternative / exceptional flows
* **3a.** The same pair must be together and apart → rejected as contradictory.
* **3b.** A together set is larger than maxGroupSize → rejected.
* **3c.** Required competency cannot be met (not enough participants with that level for every group) → warning; the teacher can keep it, and UC-14 will fail with an explanation (R13).

## Business rules / constraints
* **BR-13-1:** Constraints are hard rules (R13).
* **BR-13-2:** Constraints refer only to participants of the context.

## Domain concepts revealed
* `Constraint`, `TogetherConstraint`, `ApartConstraint`, `RequiredCompetencyConstraint`