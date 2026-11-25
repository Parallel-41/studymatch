package pt.upt.studymatch.grouping.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GroupScorerTest {

    /** Two areas keep the expected values easy to check by hand. */
    private final GroupScorer scorer = new GroupScorer(List.of("PROG", "DB"));

    private static MatchingCandidate student(long id, double prog, double db) {
        return new MatchingCandidate(id, Map.of("PROG", prog, "DB", db));
    }

    @Test
    void coverageTakesTheBestMemberInEachArea() {
        List<MatchingCandidate> group = List.of(student(1, 80, 20), student(2, 30, 70));

        assertThat(scorer.coverage(group)).isCloseTo(75, within(1e-9));
    }

    @Test
    void anAreaWithoutDataGivesNoCoverage() {
        List<MatchingCandidate> group = List.of(new MatchingCandidate(1, Map.of("PROG", 80.0)));

        assertThat(scorer.coverage(group)).isCloseTo(40, within(1e-9));
    }

    @Test
    void strengthIgnoresMembersWithoutData() {
        List<MatchingCandidate> group = List.of(student(1, 80, 20), new MatchingCandidate(2, Map.of()));

        assertThat(scorer.strength(group)).isCloseTo(50, within(1e-9));
    }

    @Test
    void aGroupWithoutAnyDataHasNoStrengthAndNoCoverage() {
        List<MatchingCandidate> group = List.of(new MatchingCandidate(1, Map.of()), new MatchingCandidate(2, Map.of()));

        assertThat(scorer.strength(group)).isZero();
        assertThat(scorer.coverage(group)).isZero();
    }

    @Test
    void balancedGroupsScoreTheirMeanCoverage() {
        List<List<MatchingCandidate>> groups = List.of(
                List.of(student(1, 90, 30), student(2, 30, 90)),
                List.of(student(3, 90, 30), student(4, 30, 90)));

        assertThat(scorer.score(groups)).isCloseTo(90, within(1e-9));
    }

    @Test
    void unbalancedGroupsLoseHalfTheSpread() {
        // coverage 80 and 40 -> mean 60; strength 80 and 40 -> standard deviation 20
        List<List<MatchingCandidate>> groups = List.of(
                List.of(student(1, 80, 80)),
                List.of(student(2, 40, 40)));

        assertThat(scorer.score(groups)).isCloseTo(60 - 0.5 * 20, within(1e-9));
    }

    @Test
    void complementaryGroupsScoreHigherThanHomogeneousOnes() {
        MatchingCandidate coderA = student(1, 90, 30);
        MatchingCandidate coderB = student(2, 90, 30);
        MatchingCandidate dbA = student(3, 30, 90);
        MatchingCandidate dbB = student(4, 30, 90);

        double homogeneous = scorer.score(List.of(List.of(coderA, coderB), List.of(dbA, dbB)));
        double complementary = scorer.score(List.of(List.of(coderA, dbA), List.of(coderB, dbB)));

        assertThat(homogeneous).isCloseTo(60, within(1e-9));
        assertThat(complementary).isGreaterThan(homogeneous);
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({"78.16, 78.2", "78.14, 78.1", "0, 0", "100, 100"})
    void roundsToOneDecimal(double value, double expected) {
        assertThat(GroupScorer.round(value)).isEqualTo(expected);
    }

    @Test
    void usesTheSixAreasOfTheProfileByDefault() {
        assertThat(new GroupScorer().areaCodes()).containsExactly("PROG", "DB", "WEB", "QUAL", "MATH", "SOFT");
    }

    @Test
    void rejectsNoAreasAndNoGroups() {
        assertThatThrownBy(() -> new GroupScorer(List.of())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> scorer.score(List.of())).isInstanceOf(IllegalArgumentException.class);
    }
}
