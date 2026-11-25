package pt.upt.studymatch.grouping.domain;

/** Fewer eligible students than the smallest group (ADR-0004, D2). Mapped to 422 by the API. */
public class NotEnoughStudentsException extends RuntimeException {

    public NotEnoughStudentsException(int students, int minimum) {
        super("At least " + minimum + " students are needed to form groups, got " + students);
    }
}
