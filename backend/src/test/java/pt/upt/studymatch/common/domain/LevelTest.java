package pt.upt.studymatch.common.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LevelTest {

    @ParameterizedTest
    @ValueSource(doubles = {0, 50.5, 100})
    void acceptsLevelsWithinRange(double value) {
        assertThatCode(() -> new Level(value)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 100.1, Double.NaN})
    void rejectsLevelsOutsideRange(double value) {
        assertThatThrownBy(() -> new Level(value)).isInstanceOf(IllegalArgumentException.class);
    }
}
