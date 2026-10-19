package pt.upt.studymatch.student.application;

/** Application-layer input for creating or updating a student. */
public record StudentCommand(
    String number,
    String name,
    String email,
    Integer registrationYear,
    String degreeProgramCode
) {
}
