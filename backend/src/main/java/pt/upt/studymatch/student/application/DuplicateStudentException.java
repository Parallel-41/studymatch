package pt.upt.studymatch.student.application;

public class DuplicateStudentException extends RuntimeException {

    public DuplicateStudentException() {
        super("A student with the same number or email already exists.");
    }
}
