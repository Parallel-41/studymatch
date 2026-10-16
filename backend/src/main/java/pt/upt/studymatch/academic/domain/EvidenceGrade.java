package pt.upt.studymatch.academic.domain;

import java.util.Objects;
import pt.upt.studymatch.common.domain.Grade;

/** Best passing grade of one course unit, as the profile needs it. */
public record EvidenceGrade(String courseUnitCode, int ects, Grade grade, int semestersAgo) {
    public EvidenceGrade {
        if (courseUnitCode == null || courseUnitCode.isBlank()) {
            throw new IllegalArgumentException("Course unit code is required");
        }
        if (ects <= 0) {
            throw new IllegalArgumentException("ECTS must be positive");
        }
        Objects.requireNonNull(grade, "grade");
        if (semestersAgo < 0) {
            throw new IllegalArgumentException("semestersAgo cannot be negative");
        }
    }
}
