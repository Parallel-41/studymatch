package pt.upt.studymatch.common.domain;

public record Level(double value) {
    public Level {
        if (Double.isNaN(value) || value < 0 || value > 100) {
            throw new IllegalArgumentException("Level must be between 0 and 100, got " + value);
        }
    }
}
