package pt.upt.studymatch.common.domain;

public record Confidence(double value) {
    public static final Confidence NONE = new Confidence(0);

    public Confidence {
        if (Double.isNaN(value) || value < 0 || value > 1) {
            throw new IllegalArgumentException("Confidence must be between 0 and 1, got " + value);
        }
    }
}
