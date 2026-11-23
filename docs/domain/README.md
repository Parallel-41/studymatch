# Domain Model (v3)

```mermaid
classDiagram
    %% Academic Core
    class DegreeProgram {
        -code : String
        -name : String
        -totalCredits : int
    }
    class CourseUnit {
        -code : String
        -name : String
        -curricularYear : int
        -semester : int
        -ects : int
    }
    class Student {
        -number : String
        -name : String
        -email : String
        -registrationYear : int
    }

    %% Academic Trajectory
    class Enrolment {
        -academicYear : String
        -status : String
    }
    class Attempt {
        -season : String
        -grade : int [0-20]
    }

    %% Competencies & Profile (Computed on Demand)
    class CompetencyArea {
        -code : String
        -name : String
        -description : String
    }
    class Competency {
        -code : String
        -name : String
    }
    class CourseUnitCompetency {
        -weight : double ]0, 1]
    }
    class SelfAssessment {
        -level : int [0-100]
        -assessedAt : LocalDate
    }
    class StudentProfile {
        -computedAt : Instant
    }
    class CompetencyLevel {
        -level : Double [0-100]
        -confidence : double [0-1]
    }

    %% Grouping & Teacher Constraints (ADR-0004)
    class GroupingContext {
        -name : String
        -description : String
        -createdAt : Instant
    }
    class GroupingConstraint {
        -type : ConstraintType
    }
    class GroupGeneration {
        -generatedAt : Instant
        -seed : long
        -score : double [0-100]
        -warning : String
    }
    class Group {
        -groupNumber : int
        -score : double [0-100]
        -explanation : String
    }

    %% Relationships
    DegreeProgram "1" -- "1..*" CourseUnit
    DegreeProgram "1" -- "0..*" Student
    Student "1" -- "0..*" Enrolment
    CourseUnit "1" -- "0..*" Enrolment
    Enrolment "1" -- "0..*" Attempt

    CompetencyArea "1" -- "1..*" Competency
    CourseUnit "1" -- "0..*" CourseUnitCompetency
    CourseUnitCompetency "0..*" -- "1" Competency
    Student "1" -- "0..6" SelfAssessment
    SelfAssessment "0..*" -- "1" CompetencyArea
    Student "1" -- "1" StudentProfile
    StudentProfile "1" -- "0..*" CompetencyLevel
    CompetencyLevel "0..*" -- "1" Competency

    GroupingContext "0..*" -- "0..*" Student : participants
    GroupingContext "1" -- "0..*" GroupingConstraint
    GroupingConstraint "0..*" -- "2..*" Student
    GroupingContext "1" -- "0..*" GroupGeneration
    GroupGeneration "1" -- "1..*" Group
    Group "0..*" -- "3..5" Student : members
