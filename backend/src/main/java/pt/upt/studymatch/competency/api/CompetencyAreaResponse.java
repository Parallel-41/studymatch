package pt.upt.studymatch.competency.api;

import java.util.List;

public record CompetencyAreaResponse(
        String code,
        String name,
        List<CompetencyResponse> competencies) {
}