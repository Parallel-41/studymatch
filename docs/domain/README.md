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
    
## Business Rules

* **R1:** La nota de aprobado es ≥ 10.
* **R2:** Cuenta la mejor nota aprobada de cada UC (Unidad Curricular).
* **R3:** El perfil se calcula en cada petición (A4 - no se almacena en base de datos).
* **R4:** score = 50 + (nota − 10) × 5 (F1).
* **R5:** Los suspensos no cuentan (F2).
* **R6:** Recencia = 0.85^semestres (F3).
* **R7:** α = min(1, ECTS ponderados / 12) y confianza = α (F4).
* **R8:** El estado "sin datos" se representa como `null`, nunca como 0.
