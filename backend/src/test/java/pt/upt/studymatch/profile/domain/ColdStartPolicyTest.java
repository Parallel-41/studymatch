package pt.upt.studymatch.profile.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import pt.upt.studymatch.common.domain.Level;

class ColdStartPolicyTest {

    private final ColdStartPolicy policy = new ColdStartPolicy();

    @Test
    void sinDatosDevuelveNoData() {
        CompetencyLevel result = policy.blend("OOP", Optional.empty(), 0.0, Optional.empty());
        assertThat(result.hasData()).isFalse();
        assertThat(result.confidence().value()).isEqualTo(0.0);
    }

    @Test
    void soloAutoevaluacionDevuelveNivelYConfianzaCero() {
        CompetencyLevel result = policy.blend("OOP", Optional.empty(), 0.0, Optional.of(new Level(60.0)));
        assertThat(result.level().value()).isEqualTo(60.0);
        assertThat(result.confidence().value()).isEqualTo(0.0);
    }

    @Test
    void soloEvidenciaDevuelveNivelYConfianzaSegunEcts() {
        CompetencyLevel result = policy.blend("OOP", Optional.of(new Level(80.0)), 6.0, Optional.empty());
        assertThat(result.level().value()).isEqualTo(80.0);
        assertThat(result.confidence().value()).isEqualTo(0.5);
    }

    @ParameterizedTest
    @CsvSource({
        "0.0, 50.0, 0.0",
        "6.0, 65.0, 0.5",
        "12.0, 80.0, 1.0",
        "20.0, 80.0, 1.0"
    })
    void mezclaSegunUmbralDeEcts(double ects, double expectedLevel, double expectedConfidence) {
        Optional<Level> evidence = Optional.of(new Level(80.0));
        Optional<Level> selfAssessed = Optional.of(new Level(50.0));

        CompetencyLevel result = policy.blend("OOP", evidence, ects, selfAssessed);

        assertThat(result.level().value()).isEqualTo(expectedLevel);
        assertThat(result.confidence().value()).isEqualTo(expectedConfidence);
    }

    @Test
    void cumpleEjemploMatematicoDelADR0003() {
        Optional<Level> evidence = Optional.of(new Level(66.1));
        double evidenceEcts = 7.335;
        Optional<Level> selfAssessed = Optional.of(new Level(50.0));

        CompetencyLevel result = policy.blend("OOP", evidence, evidenceEcts, selfAssessed);

        assertThat(result.level().value()).isCloseTo(59.9, within(0.1));
        assertThat(result.confidence().value()).isCloseTo(0.61, within(0.02));
    }
}
