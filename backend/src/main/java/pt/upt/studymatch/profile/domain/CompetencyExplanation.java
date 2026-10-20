package pt.upt.studymatch.profile.domain;

import java.util.List;
import java.util.Objects;

/**
 * Why a competency has its level (#56): the contribution of each course unit and the share of the
 * self-assessment in the final level ({@code 1 − α} when both exist, ADR-0003 F4).
 */
public record CompetencyExplanation(CompetencyLevel level, List<Contribution> contributions,
                                    double selfAssessedShare) {

    public CompetencyExplanation {
        Objects.requireNonNull(level, "level");
        contributions = List.copyOf(contributions);
    }
}
