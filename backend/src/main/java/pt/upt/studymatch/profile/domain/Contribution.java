package pt.upt.studymatch.profile.domain;

/**
 * The part of one course unit in a competency's evidence level (ADR-0003, section 3).
 *
 * @param effectiveWeight {@code ects × weight × recency} (F3)
 * @param points          {@code effectiveWeight × score / Σ effectiveWeight}; the points of all units add up to the evidence level
 * @param share           {@code points / evidenceLevel}, 0–1; the shares of all units add up to 1
 */
public record Contribution(String courseUnitCode, int ects, int grade, double weight, double recency,
                           double effectiveWeight, double points, double share) {
}
