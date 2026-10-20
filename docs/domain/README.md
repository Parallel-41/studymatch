# Initial Domain Model

```mermaid
classDiagram
    %% Núcleo Académico
    class DegreeProgram {
        -code : String
        -name : String
        -totalCredits : int
    }
    class Teacher {
        -staffNumber : String
        -name : String
        -email : String
    }
    class CourseUnit {
        -code : String
        -name : String
        -curricularYear : int
        -semester : int
        -ectsCredits : int
    }
    class Student {
        -studentNumber : String
        -name : String
        -email : String
        -registrationYear : int
    }

    %% Trayectoria
    class Enrolment {
        -academicYear : String
        -status : String
    }
    class Attempt {
        -attemptNumber : int
        -assessmentSeason : String
        -grade : int [0-20]
        -passed : boolean
    }

    %% Competencias y Perfil
    class CompetencyArea {
        -name : String
        -description : String
    }
    class Competency {
        -code : String
        -name : String
        -category : String
        -scaleMax : int
    }
    class CourseUnitCompetency {
        -weight : double [0-1]
    }
    class StudentProfile {
        -lastUpdated : LocalDate
    }
    class CompetencyLevel {
        -evidenceLevel : double [0-100]
        -coldStartLevel : double [0-100]
        -confidence : double [0-1]
    }
    class SelfAssessment {
        -submittedAt : LocalDate
        -declaredLevel : int
        -evidenceNotes : String
    }

    %% Grupos y Restricciones
    class GroupingContext {
        -title : String
        -activityDescription : String
        -targetGroupSize : int
        -status : String
    }
    class Constraint {
        <<abstract>>
        -isMandatory : boolean
    }
    class TogetherConstraint {
    }
    class ApartConstraint {
    }
    class RequiredCompetencyConstraint {
    }
    class Group {
        -groupNumber : int
        -name : String
        -matchingScore : double
    }
    class PeerReview {
        -submittedAt : LocalDate
        -feedback : String
    }

    %% Relaciones
    Teacher "1..*" -- "1..*" DegreeProgram
    DegreeProgram "1" -- "1..*" CourseUnit
    DegreeProgram "1" -- "0..*" Student
    Student "1" -- "0..*" Enrolment
    CourseUnit "1" -- "0..*" Enrolment
    Enrolment "1" -- "0..*" Attempt
    
    CompetencyArea "1" -- "1..*" Competency
    CourseUnit "1" -- "0..*" CourseUnitCompetency
    CourseUnitCompetency "0..*" -- "1" Competency
    Student "1" -- "1" StudentProfile
    StudentProfile "1" -- "0..*" CompetencyLevel
    CompetencyLevel "0..*" -- "1" Competency
    StudentProfile "1" -- "0..*" SelfAssessment
    SelfAssessment "0..*" -- "1" Competency
    
    Teacher "1" -- "0..*" GroupingContext
    CourseUnit "1" -- "0..*" GroupingContext
    GroupingContext "1" -- "0..*" Constraint
    Constraint <|-- TogetherConstraint
    Constraint <|-- ApartConstraint
    Constraint <|-- RequiredCompetencyConstraint
    GroupingContext "1" -- "0..*" Group
    Group "0..*" -- "1..*" Student
    
    Student "1" -- "0..*" PeerReview
    PeerReview "0..*" -- "1" Student
  ```
## Business rules

The use cases (`docs/use-cases/`) refer to these rules by number.

| # | Rule |
|---|---|
| R1 | Student number and e-mail are unique. Grades are integers from 0 to 20. |
| R2 | A student has at most one enrolment per course unit and academic year (format `2024/25`). |
| R3 | Academic years and semesters must exist in the curricular plan; the enrolment status follows its attempts (passed when a grade ≥ 10 exists). |
| R4 | An improvement attempt (`MELHORIA`) is only allowed on a passed enrolment and only replaces the grade when it is higher. |
| R5 | The evidence grade of a course unit is the best passing grade (≥ 10) over all its enrolments; failed attempts give no evidence (ADR-0003, F2). |
| R6 | Every course unit contributes to at least one competency, with a weight in ]0, 1]. |
| R7 | Levels are in [0, 100] and confidence in [0, 1]. Score `50 + (grade − 10) × 5` (F1); recency `0.85^semestersAgo` (F3); `α = min(1, weightedEcts / 12)` and confidence = α (F4). |
| R8 | The profile is computed on every request and never stored (A4), so a new grade or weight is reflected immediately. |
| R9 | No evidence and no self-assessment → `level = null` ("no data", never 0); self-assessment only → confidence 0. |
| R10 | At most one self-assessment per student and area; a new submission overwrites the previous one. |
| R11 | A grouping context needs 2 ≤ min ≤ max ≤ number of participants, and only students enrolled in the course unit can take part. |
| R12 | Every participant belongs to exactly one group, and group sizes respect min and max. |
| R13 | Grouping constraints are hard rules. |
| R14 | Peer reviews are only for teammates (never oneself), one per teammate and competency. |
