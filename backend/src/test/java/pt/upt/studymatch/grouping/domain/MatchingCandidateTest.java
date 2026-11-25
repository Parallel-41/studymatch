package pt.upt.studymatch.grouping.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import pt.upt.studymatch.common.domain.Confidence;
import pt.upt.studymatch.common.domain.Level;
import pt.upt.studymatch.profile.domain.AreaLevel;
import pt.upt.studymatch.profile.domain.StudentProfile;

class MatchingCandidateTest {

    @ParameterizedTest(name = "level {0} with confidence {1} -> {2}")
    @CsvSource({
            // confidence partitions: self-assessment only, partial evidence, full evidence
            "80, 0, 40",
            "80, 0.5, 60",
            "80, 1, 80",
            // level boundaries
            "0, 1, 0",
            "100, 0, 50",
            "100, 1, 100"
    })
    void effectiveLevelWeighsTheLevelByConfidence(double level, double confidence, double expected) {
        double effective = MatchingCandidate.effectiveLevel(new Level(level), new Confidence(confidence));

        assertThat(effective).isCloseTo(expected, within(1e-9));
    }

    @Test
    void adapterKeepsOnlyAreasWithDataAndNeverTurnsThemIntoZero() {
        StudentProfile profile = new StudentProfile(List.of(
                new AreaLevel("PROG", new Level(80), new Confidence(1)),
                new AreaLevel("WEB", new Level(60), new Confidence(0)),
                AreaLevel.noData("DB")), List.of());

        MatchingCandidate candidate = MatchingCandidate.fromProfile(7, profile);

        assertThat(candidate.studentId()).isEqualTo(7);
        assertThat(candidate.effectiveLevels()).containsOnlyKeys("PROG", "WEB");
        assertThat(candidate.effectiveLevel("PROG")).hasValue(80);
        assertThat(candidate.effectiveLevel("WEB")).hasValue(30);
        assertThat(candidate.effectiveLevel("DB")).isEmpty();
    }

    @Test
    void overallIsTheMeanOfTheAreasWithData() {
        MatchingCandidate candidate = new MatchingCandidate(1, Map.of("PROG", 80.0, "DB", 40.0));

        assertThat(candidate.overall()).hasValue(60);
        assertThat(candidate.hasData()).isTrue();
    }

    @Test
    void aStudentWithoutAnyDataHasNoOverallLevel() {
        StudentProfile empty = new StudentProfile(List.of(AreaLevel.noData("PROG"), AreaLevel.noData("DB")), List.of());

        MatchingCandidate candidate = MatchingCandidate.fromProfile(3, empty);

        assertThat(candidate.hasData()).isFalse();
        assertThat(candidate.overall()).isEmpty();
    }

    @ParameterizedTest(name = "effective level {0} is rejected")
    @ValueSource(doubles = {-0.1, 100.1, Double.NaN})
    void rejectsEffectiveLevelsOutsideTheScale(double level) {
        assertThatThrownBy(() -> new MatchingCandidate(1, Map.of("PROG", level)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
