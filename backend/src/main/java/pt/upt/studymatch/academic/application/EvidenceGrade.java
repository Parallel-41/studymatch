
package pt.upt.studymatch.academic.application;

import pt.upt.studymatch.common.domain.Grade;

public record EvidenceGrade(
    Long courseUnitId,
    String courseUnitCode,
    String courseUnitName,
    Grade grade,
    int semestersAgo
) {
    public EvidenceGrade {
        if (courseUnitId == null || courseUnitCode == null
            || courseUnitName == null || grade == null) {
            throw new IllegalArgumentException(
                "Evidence-grade fields must not be null."
            );
        }

        if (!grade.isPassing()) {
            throw new IllegalArgumentException(
                "The evidence grade must be a passing grade."
            );
        }

        if (semestersAgo < 0) {
            throw new IllegalArgumentException(
                "semestersAgo must not be negative."
            );
        }
    }
}
