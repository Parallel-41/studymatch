package pt.upt.studymatch.profile.domain;

import java.util.Objects;
import pt.upt.studymatch.common.domain.Confidence;
import pt.upt.studymatch.common.domain.Level;

/** Computed level of one competency. level == null means "no data", never 0. */
public record CompetencyLevel(String competencyCode, Level level, Confidence confidence,
                              Level evidenceLevel, Level selfAssessedLevel) {
    public CompetencyLevel {
        Objects.requireNonNull(competencyCode, "competencyCode");
        Objects.requireNonNull(confidence, "confidence");
    }

    public static CompetencyLevel noData(String competencyCode) {
        return new CompetencyLevel(competencyCode, null, Confidence.NONE, null, null);
    }

    public boolean hasData() {
        return level != null;
    }
}
