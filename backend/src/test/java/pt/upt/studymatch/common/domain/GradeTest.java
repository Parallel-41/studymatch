package pt.upt.studymatch.common.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class GradeTest {

    @ParameterizedTest
    @ValueSource(ints = {0, 10, 20})
    void acceptsGradesWithinRange(int value) {
        assertThat(new Grade(value).value()).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 21})
    void rejectsGradesOutsideRange(int value) {
        assertThatThrownBy(() -> new Grade(value)).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({"9,false", "10,true", "20,true", "0,false"})
    void passesFromTen(int value, boolean passing) {
        assertThat(new Grade(value).isPassing()).isEqualTo(passing);
    }
}
