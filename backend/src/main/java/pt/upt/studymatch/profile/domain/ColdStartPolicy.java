package pt.upt.studymatch.profile.domain;

import pt.upt.studymatch.common.domain.Confidence;
import pt.upt.studymatch.common.domain.Level;

import java.util.Optional;

public class ColdStartPolicy {

    private static final double ECTS_THRESHOLD = 12.0;

    public CompetencyLevel blend(String competencyCode, Optional<Level> evidence, double evidenceEcts, Optional<Level> selfAssessed) {

        if (evidence.isEmpty() && selfAssessed.isEmpty()) {
            return CompetencyLevel.noData(competencyCode);
        }

        double alpha = Math.min(1.0, evidenceEcts / ECTS_THRESHOLD);
        alpha = Math.max(0.0, alpha);

        Level finalLevel;
        Confidence confidence = new Confidence(alpha);

        if (evidence.isEmpty()) {
            finalLevel = selfAssessed.get();
            confidence = Confidence.NONE;
        } else if (selfAssessed.isEmpty()) {
            finalLevel = evidence.get();
        } else {
            double blendedValue = (alpha * evidence.get().value()) + ((1.0 - alpha) * selfAssessed.get().value());
            finalLevel = new Level(blendedValue);
        }

        return new CompetencyLevel(
            competencyCode,
            finalLevel,
            confidence,
            evidence.orElse(null),
            selfAssessed.orElse(null)
        );
    }
}
