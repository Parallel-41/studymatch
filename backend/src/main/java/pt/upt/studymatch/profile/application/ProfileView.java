package pt.upt.studymatch.profile.application;

import java.util.List;

/** The computed profile with the catalogue names, ready to be mapped to the API. */
public record ProfileView(List<AreaView> areas, List<CompetencyView> competencies) {

    public ProfileView {
        areas = List.copyOf(areas);
        competencies = List.copyOf(competencies);
    }

    /** level == null means "no data", never 0. */
    public record AreaView(String code, String name, Double level, double confidence) {
    }

    /** level, evidenceLevel and selfAssessedLevel are null when there is no data. */
    public record CompetencyView(String code, String name, String area, Double level, double confidence,
                                 Double evidenceLevel, Double selfAssessedLevel) {
    }
}
