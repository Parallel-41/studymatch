package pt.upt.studymatch.profile.application;

import java.util.List;
import pt.upt.studymatch.profile.domain.Contribution;

/** The explanation of one competency, ready to be mapped to the API. Levels are null when there is no data. */
public record ExplanationView(String competencyCode, String competencyName, Double level, double confidence,
                              Double evidenceLevel, Double selfAssessedLevel, double selfAssessedShare,
                              List<Contribution> contributions) {

    public ExplanationView {
        contributions = List.copyOf(contributions);
    }
}
