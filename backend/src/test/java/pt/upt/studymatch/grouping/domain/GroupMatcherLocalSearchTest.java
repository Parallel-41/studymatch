package pt.upt.studymatch.grouping.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static pt.upt.studymatch.grouping.domain.GroupMatcherTest.students;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class GroupMatcherLocalSearchTest {

    private final GroupScorer scorer = new GroupScorer();
    private final GroupMatcher matcher = new GroupMatcher();

    /** The baseline: students cut into the planned sizes in the order they were listed. */
    private static List<List<MatchingCandidate>> naiveSplit(List<MatchingCandidate> students) {
        List<List<MatchingCandidate>> groups = new ArrayList<>();
        int start = 0;
        for (int size : new GroupSizePlanner().plan(students.size()).sizes()) {
            groups.add(new ArrayList<>(students.subList(start, start + size)));
            start += size;
        }
        return groups;
    }

    private List<List<MatchingCandidate>> seeded(List<MatchingCandidate> students, long seed) {
        List<Integer> sizes = new GroupSizePlanner().plan(students.size()).sizes();
        return matcher.seed(students, sizes, new Random(seed));
    }

    @ParameterizedTest(name = "{0} students, data seed {1}")
    @CsvSource({"13, 1", "13, 2", "23, 1", "23, 2", "23, 3", "40, 1", "40, 2"})
    void scoresHigherThanANaiveSplit(int count, long dataSeed) {
        List<MatchingCandidate> students = students(count, dataSeed);

        double naive = scorer.score(naiveSplit(students));
        double matched = matcher.match(students, 42).score();

        assertThat(matched).isGreaterThan(naive);
    }

    @Test
    void turnsHomogeneousGroupsIntoComplementaryOnes() {
        GroupScorer twoAreas = new GroupScorer(List.of("PROG", "DB"));
        GroupMatcher twoAreaMatcher = new GroupMatcher(new GroupSizePlanner(), twoAreas);
        List<MatchingCandidate> students = new ArrayList<>();
        for (long id = 1; id <= 4; id++) {
            students.add(new MatchingCandidate(id, Map.of("PROG", 90.0, "DB", 30.0)));
        }
        for (long id = 5; id <= 8; id++) {
            students.add(new MatchingCandidate(id, Map.of("PROG", 30.0, "DB", 90.0)));
        }

        GroupAssignment assignment = twoAreaMatcher.match(students, 3);

        assertThat(twoAreas.score(naiveSplit(students))).isCloseTo(60, within(1e-9));
        assertThat(assignment.score()).isCloseTo(90, within(1e-9));
        assertThat(assignment.groups()).allSatisfy(group -> assertThat(group.studentIds())
                .anyMatch(id -> id <= 4)
                .anyMatch(id -> id > 4));
    }

    @ParameterizedTest(name = "seed {0}")
    @ValueSource(longs = {1, 2, 3, 42, 2026})
    void neverLowersTheScoreOfTheSeeding(long seed) {
        List<List<MatchingCandidate>> groups = seeded(students(23, seed), seed);
        double before = scorer.score(groups);

        matcher.improve(groups, new Random(seed));

        assertThat(scorer.score(groups)).isGreaterThanOrEqualTo(before);
    }

    @Test
    void stopsAtALocalOptimumWhereNoSingleSwapHelps() {
        List<List<MatchingCandidate>> groups = seeded(students(13, 5), 5);

        int passes = matcher.improve(groups, new Random(5));
        double best = scorer.score(groups);

        assertThat(passes).isBetween(1, GroupMatcher.MAX_PASSES);
        for (int i = 0; i < groups.size(); i++) {
            for (int j = i + 1; j < groups.size(); j++) {
                for (int a = 0; a < groups.get(i).size(); a++) {
                    for (int b = 0; b < groups.get(j).size(); b++) {
                        assertThat(scoreAfterSwap(groups, i, a, j, b)).isLessThanOrEqualTo(best + 1e-9);
                    }
                }
            }
        }
    }

    @Test
    void keepsEveryStudentAndTheSizesAfterSwapping() {
        List<MatchingCandidate> students = students(23, 9);
        List<List<MatchingCandidate>> groups = seeded(students, 9);

        matcher.improve(groups, new Random(9));

        assertThat(groups).extracting(List::size).containsExactly(5, 5, 5, 4, 4);
        assertThat(groups.stream().flatMap(List::stream).toList())
                .doesNotHaveDuplicates()
                .containsExactlyInAnyOrderElementsOf(students);
    }

    private double scoreAfterSwap(List<List<MatchingCandidate>> groups, int i, int a, int j, int b) {
        List<List<MatchingCandidate>> copy = new ArrayList<>();
        groups.forEach(group -> copy.add(new ArrayList<>(group)));
        MatchingCandidate moved = copy.get(i).get(a);
        copy.get(i).set(a, copy.get(j).get(b));
        copy.get(j).set(b, moved);
        return scorer.score(copy);
    }
}
