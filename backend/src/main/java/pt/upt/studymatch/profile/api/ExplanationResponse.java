package pt.upt.studymatch.profile.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import pt.upt.studymatch.profile.application.ExplanationView;

/**
 * JSON contract of {@code GET /api/students/{id}/profile/competencies/{code}/explanation}, same structure as
 * {@code frontend/src/api/examples/explanation.json}. Levels and points have 1 decimal; confidence, recency
 * and shares are 0–1 with 2 decimals.
 */
public record ExplanationResponse(String competencyCode, Double level, double confidence,
                                  Double evidenceLevel, Double selfAssessedLevel, double selfAssessedShare,
                                  List<ContributionResponse> contributions) {

    public record ContributionResponse(String courseUnitCode, int ects, int grade, double weight, double recency,
                                       double points, double share) {
    }

    public static ExplanationResponse from(ExplanationView view) {
        return new ExplanationResponse(view.competencyCode(),
            ProfileResponse.level(view.level()), ProfileResponse.confidence(view.confidence()),
            ProfileResponse.level(view.evidenceLevel()), ProfileResponse.level(view.selfAssessedLevel()),
            round(view.selfAssessedShare(), 2),
            view.contributions().stream()
                .map(c -> new ContributionResponse(c.courseUnitCode(), c.ects(), c.grade(), round(c.weight(), 2),
                    round(c.recency(), 2), round(c.points(), 1), round(c.share(), 2)))
                .toList());
    }

    private static double round(double value, int decimals) {
        return BigDecimal.valueOf(value).setScale(decimals, RoundingMode.HALF_UP).doubleValue();
    }
}
