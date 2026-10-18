package pt.upt.studymatch.competency.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.upt.studymatch.competency.domain.CourseUnitCompetency;
import pt.upt.studymatch.competency.domain.CourseUnitWeight;
import pt.upt.studymatch.competency.infrastructure.CompetencyAreaRepository;
import pt.upt.studymatch.competency.infrastructure.CompetencyRepository;
import pt.upt.studymatch.competency.infrastructure.CourseUnitCompetencyRepository;

@ExtendWith(MockitoExtension.class)
class CompetencyCatalogueServiceTest {

    @Mock
    private CompetencyAreaRepository areaRepository;
    @Mock
    private CompetencyRepository competencyRepository;
    @Mock
    private CourseUnitCompetencyRepository courseUnitCompetencyRepository;

    @InjectMocks
    private CompetencyCatalogueService service;

    @Test
    void weightsFor_withTwoCompetencies_shouldReturnTwoWeights() {
        // Simulamos una asignatura con 2 competencias
        when(courseUnitCompetencyRepository.findByCourseUnitCodeIn(List.of("PROG1")))
                .thenReturn(List.of(
                        new CourseUnitCompetency("PROG1", "PROG_OO", 0.6),
                        new CourseUnitCompetency("PROG1", "PROG_ALG", 0.4)
                ));

        List<CourseUnitWeight> result = service.weightsFor(List.of("PROG1"));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CourseUnitWeight::competencyCode)
                .containsExactlyInAnyOrder("PROG_OO", "PROG_ALG");
    }

    @Test
    void weightsFor_withUnknownUnit_shouldReturnEmptyList() {
        // Simulamos una asignatura desconocida
        when(courseUnitCompetencyRepository.findByCourseUnitCodeIn(List.of("UNKNOWN")))
                .thenReturn(List.of());

        List<CourseUnitWeight> result = service.weightsFor(List.of("UNKNOWN"));

        assertThat(result).isEmpty();
    }
}