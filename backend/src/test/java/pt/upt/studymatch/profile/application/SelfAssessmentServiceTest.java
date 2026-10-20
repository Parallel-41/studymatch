package pt.upt.studymatch.profile.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import pt.upt.studymatch.competency.infrastructure.CompetencyAreaRepository;
import pt.upt.studymatch.profile.infrastructure.SelfAssessmentRepository;

class SelfAssessmentServiceTest {

    private SelfAssessmentService service;
    private SelfAssessmentRepository repository;
    private CompetencyAreaRepository areaRepository;
    private Clock clock;

    @BeforeEach
    void setUp() {
        // Simulamos las dependencias
        repository = mock(SelfAssessmentRepository.class);
        areaRepository = mock(CompetencyAreaRepository.class);

        // Reloj fijo para que los tests sean deterministas
        clock = Clock.fixed(Instant.parse("2026-10-20T10:00:00Z"), ZoneId.of("UTC"));

        // Enseñamos al mock a reconocer las áreas de los tests
        when(areaRepository.existsByCode("PROG")).thenReturn(true);
        when(areaRepository.existsByCode("DB")).thenReturn(true);
        when(areaRepository.existsByCode("FÍSICA")).thenReturn(false);

        // Pasamos los 3 parámetros que ahora exige el constructor
        service = new SelfAssessmentService(repository, areaRepository, clock);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 50.5, 100.0})
    void aceptaNivelesValidosYAreasConocidas(double level) {
        Map<String, Double> validData = Map.of("PROG", level, "DB", level);

        // No debería lanzar ninguna excepción
        assertThatCode(() -> service.saveSelfAssessments(1L, validData))
            .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 100.1})
    void rechazaNivelesFueraDeRango(double invalidLevel) {
        Map<String, Double> invalidData = Map.of("PROG", invalidLevel);

        assertThatThrownBy(() -> service.saveSelfAssessments(1L, invalidData))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Nivel fuera de rango");
    }

    @Test
    void rechazaAreaDesconocida() {
        Map<String, Double> invalidData = Map.of("FÍSICA", 50.0);

        assertThatThrownBy(() -> service.saveSelfAssessments(1L, invalidData))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Área desconocida");
    }
}
