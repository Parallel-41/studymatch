package pt.upt.studymatch.profile.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import pt.upt.studymatch.profile.application.ProfileView;

/**
 * JSON contract of {@code GET /api/students/{id}/profile}, same structure as
 * {@code frontend/src/api/examples/profile.json}. level is 0–100 with 1 decimal,
 * confidence 0–1 with 2 decimals, and level == null means "no data".
 */
public record ProfileResponse(List<AreaResponse> areas, List<CompetencyResponse> competencies) {

    public record AreaResponse(String code, String name, Double level, double confidence) {
    }

    public record CompetencyResponse(String code, String name, String area, Double level, double confidence,
                                     Double evidenceLevel, Double selfAssessedLevel) {
    }

    public static ProfileResponse from(ProfileView view) {
        return new ProfileResponse(
            view.areas().stream()
                .map(a -> new AreaResponse(a.code(), a.name(), level(a.level()), confidence(a.confidence())))
                .toList(),
            view.competencies().stream()
                .map(c -> new CompetencyResponse(c.code(), c.name(), c.area(), level(c.level()),
                    confidence(c.confidence()), level(c.evidenceLevel()), level(c.selfAssessedLevel())))
                .toList());
    }

    static Double level(Double value) {
        return value == null ? null : round(value, 1);
    }

    static double confidence(double value) {
        return round(value, 2);
    }

    private static double round(double value, int decimals) {
        return BigDecimal.valueOf(value).setScale(decimals, RoundingMode.HALF_UP).doubleValue();
    }
}
