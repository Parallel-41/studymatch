# UC-12 — Create a grouping context

**Objective:** Describe the situation where groups will be formed: UC, activity, participants, size and goals.  
**Primary actor(s):** Teacher  
**Capability area:** Grouping context  
**Target sprint:** 4  
**Preconditions:** Teacher teaches the UC.  
**Postconditions:** Context saved as DRAFT or READY.  

## Main success scenario
1. Teacher chooses New grouping context.
2. Teacher fills in name, activity, UC, academic year, min and max group size.
3. System pre-selects as participants the students enrolled in that UC and year; Teacher can remove some.
4. Teacher keeps or adjusts the goal weights (coverage, balance, repeated teammates).
5. System validates and saves the context as `DRAFT`.

## Alternative / exceptional flows
* **5a.** min > max, min < 2 or max greater than the number of participants → rejected (R11).
* **3a.** A student not enrolled in the UC is added → rejected (R11).
* **5b.** Teacher marks the context `READY` → it can be used by UC-14.

## Business rules / constraints
* **BR-12-1:** 2 ≤ minGroupSize ≤ maxGroupSize (R11).
* **BR-12-2:** Weights in [0, 1] with sensible defaults (ADR-0003 §4).

## Domain concepts revealed
* `GroupingContext`, `ContextStatus`, `AcademicYear`