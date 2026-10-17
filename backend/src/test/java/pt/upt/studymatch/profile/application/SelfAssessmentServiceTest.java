package pt.upt.studymatch.profile.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import pt.upt.studymatch.profile.infrastructure.SelfAssessmentRepository;

class SelfAssessmentServiceTest {

    private SelfAssessmentService service;
    private SelfAssessmentRepository repository;

    @BeforeEach
    void setUp() {
        // Simulamos la base de datos para probar solo la lógica pura del servicio
        repository = mock(SelfAssessmentRepository.class);
        service = new SelfAssessmentService(repository);
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
