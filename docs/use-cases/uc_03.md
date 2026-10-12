# UC-03 — Sign in and access by role

**Objective:** Give each user only the functions of their role.  
**Primary actor(s):** Student, Teacher, Admin  
**Capability area:** Identity  
**Target sprint:** 3  
**Preconditions:** An account exists for the user.  
**Postconditions:** User has a session with the permissions of their role.  

## Main success scenario
1. User opens StudyMatch and enters e-mail and password.
2. System checks the credentials and that the account is active.
3. System opens the home page of the user's role: Student → my profile; Teacher → my grouping contexts; Admin → data management.

## Alternative / exceptional flows
* **2a.** Wrong credentials → generic invalid e-mail or password message (no hint about which one).
* **2b.** Account inactive → access denied, message to contact the administrator.
* **3a.** User calls a function of another role → HTTP 403.

## Business rules / constraints
* **BR-03-1:** Passwords are stored only as hashes.
* **BR-03-2:** A student sees only their own profile and trajectory; teachers see students of their contexts.
* **BR-03-3:** Authentication is behind an interface so university SSO can replace it (ADR-0003 §6).

## Domain concepts revealed
* `UserAccount`, `Role`