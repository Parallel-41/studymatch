package pt.upt.studymatch.competency.application;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.competency.api.CompetencyAreaResponse;
import pt.upt.studymatch.competency.api.CompetencyResponse;
import pt.upt.studymatch.competency.domain.Competency;
import pt.upt.studymatch.competency.domain.CompetencyArea;
import pt.upt.studymatch.competency.domain.CourseUnitWeight;
import pt.upt.studymatch.competency.infrastructure.CompetencyAreaRepository;
import pt.upt.studymatch.competency.infrastructure.CompetencyRepository;
import pt.upt.studymatch.competency.infrastructure.CourseUnitCompetencyRepository;

@Service
@Transactional(readOnly = true)
public class CompetencyCatalogueService {

    private final CompetencyAreaRepository areaRepository;
    private final CompetencyRepository competencyRepository;
    private final CourseUnitCompetencyRepository courseUnitCompetencyRepository;

    public CompetencyCatalogueService(
            CompetencyAreaRepository areaRepository,
            CompetencyRepository competencyRepository,
            CourseUnitCompetencyRepository courseUnitCompetencyRepository) {
        this.areaRepository = areaRepository;
        this.competencyRepository = competencyRepository;
        this.courseUnitCompetencyRepository = courseUnitCompetencyRepository;
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

    // NUEVO MÉTODO PARA EL ISSUE #47
    public List<CourseUnitWeight> weightsFor(Collection<String> courseUnitCodes) {
        return courseUnitCompetencyRepository.findByCourseUnitCodeIn(courseUnitCodes).stream()
                .map(cuc -> new CourseUnitWeight(cuc.getCourseUnitCode(), cuc.getCompetencyCode(), cuc.getWeight()))
                .toList();
    }
}