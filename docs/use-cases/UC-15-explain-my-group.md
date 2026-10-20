# UC-15 — Explain my group

**Objective:** Show a student why they were placed in their group.  
**Primary actor(s):** Student  
**Capability area:** Proposal  
**Target sprint:** 4  
**Preconditions:** Student belongs to a formed group.  
**Postconditions:** —  

## Main success scenario
1. Student opens My groups and selects a group.
2. System shows the members and, for the student, the explanation: which required competencies they cover, how they balance the group, and which constraints applied to them.

## Alternative / exceptional flows
* **2a.** Formation was edited manually by the teacher → explanation says placed by the teacher.
* **1a.** Context not yet formed → no group shown.

## Business rules / constraints
* **BR-15-1:** The explanation never shows other students' levels — only competencies covered (privacy).
* **BR-15-2:** Explanation is generated when the formation is saved (`GroupMembership.explanation`).

## Domain concepts revealed
* `GroupMembership`, `Group`, `Constraint`

## Why it adds value
Automatic groups are often distrusted when the criteria are hidden. Showing why turns the algorithm into something students understand and accept, makes it easier to take a role in the team, and gives the teacher a way to justify the groups. It also forces the matching to be explainable, which keeps it testable.