package pt.upt.studymatch.common.domain;

/**
 * A grade on the Portuguese 0–20 scale.
 */
public record Grade(int value) {

    public static final int MIN = 0;
    public static final int MAX = 20;
    public static final int PASS_MARK = 10;

    public Grade {
        if (value < MIN || value > MAX) {
            throw new IllegalArgumentException(
                "Grade must be between 0 and 20, got " + value
            );
        }
    }

    public boolean isPassing() {
        return value >= PASS_MARK;
    }
}
