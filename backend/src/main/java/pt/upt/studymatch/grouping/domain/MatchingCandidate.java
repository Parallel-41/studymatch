package pt.upt.studymatch.grouping.domain;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import pt.upt.studymatch.common.domain.Confidence;
import pt.upt.studymatch.common.domain.Level;
import pt.upt.studymatch.profile.domain.AreaLevel;
import pt.upt.studymatch.profile.domain.StudentProfile;

/**
 * What the matcher knows about one student: the effective level per competency area (ADR-0004, input).
 *
 * <p>{@code effective = level × (0.5 + 0.5 × confidence)}: a level backed by grades counts fully, a
 * self-assessed one (cold start) counts half. An area without data is simply absent from the map; it is
 * never turned into level 0 (R9).
 */
public record MatchingCandidate(long studentId, Map<String, Double> effectiveLevels) {

    /** Share of the level that counts even with confidence 0. */
    public static final double CONFIDENCE_FLOOR = 0.5;

    public MatchingCandidate {
        Objects.requireNonNull(effectiveLevels, "effectiveLevels");
        effectiveLevels.forEach((area, level) -> {
            if (level == null || level.isNaN() || level < 0 || level > 100) {
                throw new IllegalArgumentException(
                        "Effective level of " + area + " must be between 0 and 100, got " + level);
            }
        });
        effectiveLevels = Map.copyOf(effectiveLevels);
    }

    /** Adapter from the Sprint 2 profile (ADR-0003); areas without data are left out. */
    public static MatchingCandidate fromProfile(long studentId, StudentProfile profile) {
        Objects.requireNonNull(profile, "profile");
        Map<String, Double> levels = new LinkedHashMap<>();
        for (AreaLevel area : profile.areas()) {
            if (area.hasData()) {
                levels.put(area.areaCode(), effectiveLevel(area.level(), area.confidence()));
            }
        }
        return new MatchingCandidate(studentId, levels);
    }

    public static double effectiveLevel(Level level, Confidence confidence) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(confidence, "confidence");
        return level.value() * (CONFIDENCE_FLOOR + (1 - CONFIDENCE_FLOOR) * confidence.value());
    }

    public OptionalDouble effectiveLevel(String areaCode) {
        Double level = effectiveLevels.get(areaCode);
        return level == null ? OptionalDouble.empty() : OptionalDouble.of(level);
    }

    /** Mean effective level over the areas with data; empty for a student without any data. */
    public OptionalDouble overall() {
        return effectiveLevels.values().stream().mapToDouble(Double::doubleValue).average();
    }

    public boolean hasData() {
        return !effectiveLevels.isEmpty();
    }
}
