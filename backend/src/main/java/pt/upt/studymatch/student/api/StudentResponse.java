package pt.upt.studymatch.student.api;

import pt.upt.studymatch.student.domain.Student;

import java.time.Clock;

/** Explicit JSON contract for a student, independent of the JPA entity graph. */
public record StudentResponse(
    Long id,
    String number,
    String name,
    String email,
    Integer registrationYear,
    int currentYear,
    DegreeProgramResponse degreeProgram
) {

    public static StudentResponse from(Student student, Clock clock) {
        return new StudentResponse(
            student.getId(),
            student.getNumber(),
            student.getName(),
            student.getEmail(),
            student.getRegistrationYear(),
            student.currentYear(clock),
            new DegreeProgramResponse(
                student.getDegreeProgram().getCode(),
                student.getDegreeProgram().getName()
            )
        );
    }

    public record DegreeProgramResponse(String code, String name) {
    }
}
