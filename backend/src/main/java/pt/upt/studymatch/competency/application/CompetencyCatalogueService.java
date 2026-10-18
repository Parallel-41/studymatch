package pt.upt.studymatch.competency.application;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.competency.api.CompetencyAreaResponse;
import pt.upt.studymatch.competency.api.CompetencyResponse;
import pt.upt.studymatch.competency.domain.Competency;
import pt.upt.studymatch.competency.domain.CompetencyArea;
import pt.upt.studymatch.competency.infrastructure.CompetencyAreaRepository;
import pt.upt.studymatch.competency.infrastructure.CompetencyRepository;

@Service
@Transactional(readOnly = true)
public class CompetencyCatalogueService {

    private final CompetencyAreaRepository areaRepository;
    private final CompetencyRepository competencyRepository;

    public CompetencyCatalogueService(
            CompetencyAreaRepository areaRepository,
            CompetencyRepository competencyRepository) {
        this.areaRepository = areaRepository;
        this.competencyRepository = competencyRepository;
    }

    public List<CompetencyAreaResponse> findAllByArea() {
        List<CompetencyArea> areas = areaRepository.findAllByOrderByIdAsc();
        List<Competency> competencies = competencyRepository.findAllByOrderByIdAsc();

        Map<Long, List<CompetencyResponse>> competenciesByAreaId = competencies.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getArea().getId(),
                        Collectors.mapping(
                                c -> new CompetencyResponse(c.getCode(), c.getName()),
                                Collectors.toList())));

        return areas.stream()
                .map(area -> new CompetencyAreaResponse(
                        area.getCode(),
                        area.getName(),
                        competenciesByAreaId.getOrDefault(area.getId(), List.of())))
                .toList();
    }
}