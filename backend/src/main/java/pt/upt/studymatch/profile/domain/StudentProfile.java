package pt.upt.studymatch.profile.domain;

import java.util.List;
import java.util.Optional;

/** The computed profile of a student (ADR-0003, A4: computed on demand, never stored). */
public record StudentProfile(List<AreaLevel> areas, List<CompetencyLevel> competencies) {

    public StudentProfile {
        areas = List.copyOf(areas);
        competencies = List.copyOf(competencies);
    }

    public Optional<CompetencyLevel> competency(String competencyCode) {
        return competencies.stream()
            .filter(c -> c.competencyCode().equals(competencyCode))
            .findFirst();
    }

    public Optional<AreaLevel> area(String areaCode) {
        return areas.stream()
            .filter(a -> a.areaCode().equals(areaCode))
            .findFirst();
    }
}
