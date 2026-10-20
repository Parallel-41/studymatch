# UC-16 — Review my teammates' soft skills

**Objective:** After a group activity, let members rate each other's soft skills and use it as evidence for the Soft skills area.  
**Primary actor(s):** Student  
**Capability area:** Proposal  
**Target sprint:** 5  
**Preconditions:** Context CLOSED and student was a member.  
**Postconditions:** Reviews stored; profile may include soft-skill evidence.  

## Main success scenario
1. When a context is `CLOSED`, System invites every member to review their teammates.
2. Student rates each teammate (0–100) on each soft-skill competency (communication, teamwork, responsibility) and may add a comment.
3. Student submits.
4. System stores the reviews and, once at least two reviews exist for a reviewee, adds the average as evidence for the Soft skills competencies and recalculates the profile (UC-08).

## Alternative / exceptional flows
* **2a.** Student tries to review themselves or a non-member → not offered (R14).
* **3a.** Review already submitted for that teammate and competency → rejected (R14).
* **4a.** Fewer than two reviews for a reviewee → no evidence is added yet (avoids one-person bias).

## Business rules / constraints
* **BR-16-1:** Only members of the same group, never oneself, once per (group, reviewee, competency) — R14.
* **BR-16-2:** Individual ratings are never shown to the reviewee, only the aggregate.

## Domain concepts revealed
* `PeerReview`, `Group`, `Competency` (Soft skills area)

## Why it adds value
Grades say little about soft skills, which are exactly what makes a group work. Peer review is the only source of evidence for that area, it improves future matching, and it gives students feedback they rarely get.