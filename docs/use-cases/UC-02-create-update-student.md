# UC-02 — Create and update a student record

**Objective:** Keep a student's identity data correct: create a student that was not imported and update data that changed.  
**Primary actor(s):** Admin  
**Capability area:** Identity  
**Target sprint:** 2  
**Preconditions:** Admin is signed in.  
**Postconditions:** Student exists with valid, unique identity data.  

## Main success scenario
1. Admin opens the student list and chooses New student (or Edit on an existing one).
2. System shows the form: number, name, e-mail, degree programme, current year, admission year.
3. Admin fills in or changes the fields and saves.
4. System validates the data and saves it.
5. System shows the updated student in the list.

## Alternative / exceptional flows
* **4a.** A required field is empty, the e-mail is invalid or the current year is outside [1, programme duration] → System shows the error next to the field; nothing is saved.
* **4b.** Number or e-mail already belongs to another student → System rejects with already in use (HTTP 409).
* **1a.** Admin searches/filters the list by name, number or year before choosing a student.

## Business rules / constraints
* **BR-02-1:** Student number and e-mail are unique (R1).
* **BR-02-2:** Names and numbers are trimmed; e-mail is stored in lower case.
* **BR-02-3:** Changing identity data never changes academic history or the profile.

## Domain concepts revealed
* `Student`, `DegreeProgramme`, `Email` (value object), `AcademicYear` (value object)