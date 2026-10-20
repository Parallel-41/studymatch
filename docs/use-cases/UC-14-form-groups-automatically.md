# UC-14 — Form groups automatically

**Objective:** Generate groups that are complementary inside and balanced between each other, respecting every constraint.  
**Primary actor(s):** Teacher (System executes)  
**Capability area:** Grouping context  
**Target sprint:** 4  
**Preconditions:** Context is READY.  
**Postconditions:** Context FORMED with a saved formation.  

## Main success scenario
1. Teacher opens a `READY` context and chooses Form groups.
2. System runs the algorithm with a seed (ADR-0003 §4): greedy seeding + local search.
3. System shows the proposed groups with score, coverage and spread, and each member's explanation.
4. Teacher accepts the formation.
5. System saves the `GroupFormation` and sets the context to `FORMED`.

## Alternative / exceptional flows
* **2a.** Constraints cannot all be satisfied → no formation is saved; System explains which constraint failed (R13).
* **4a.** Teacher asks for another proposal → System runs again with a new seed.
* **4b.** Teacher moves a student manually → System re-checks constraints and recalculates the score.

## Business rules / constraints
* **BR-14-1:** Every participant is in exactly one group; sizes respect min/max (R12).
* **BR-14-2:** Same data + same seed → same formation (testable).

## Domain concepts revealed
* `GroupFormation`, `Group`, `GroupMembership`