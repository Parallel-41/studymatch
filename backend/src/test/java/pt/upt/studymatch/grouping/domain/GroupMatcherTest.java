package pt.upt.studymatch.grouping.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class GroupMatcherTest {

    private final GroupMatcher matcher = new GroupMatcher();

    /**
     * A reproducible class: every third student is a cold start (half of the areas without data) and
     * every tenth has no data at all, so R9 is exercised in every run.
     */
    static List<MatchingCandidate> students(int count, long dataSeed) {
        Random random = new Random(dataSeed);
        List<MatchingCandidate> students = new ArrayList<>(count);
        for (int id = 1; id <= count; id++) {
            Map<String, Double> levels = new HashMap<>();
            if (id % 10 != 0) {
                for (String area : GroupScorer.DEFAULT_AREAS) {
                    boolean missing = id % 3 == 0 && random.nextBoolean();
                    if (!missing) {
                        levels.put(area, 20 + random.nextInt(81) * 0.9);
                    }
                }
            }
            students.add(new MatchingCandidate(id, levels));
        }
        return students;
    }

    private static MatchingCandidate withOverall(long id, double level) {
        return new MatchingCandidate(id, Map.of("PROG", level));
    }

    @ParameterizedTest(name = "{0} students")
    @ValueSource(ints = {4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 23, 40, 101})
    void placesEveryStudentExactlyOnceInThePlannedSizes(int count) {
        List<MatchingCandidate> students = students(count, count);

        GroupAssignment assignment = matcher.match(students, 42);

        assertThat(assignment.studentIds())
                .doesNotHaveDuplicates()
                .containsExactlyInAnyOrderElementsOf(students.stream().map(MatchingCandidate::studentId).toList());
        assertThat(assignment.sizes()).isEqualTo(new GroupSizePlanner().plan(count).sizes());
        assertThat(assignment.groups()).extracting(MatchedGroup::number)
                .containsExactlyElementsOf(IntStream.rangeClosed(1, assignment.groups().size()).boxed().toList());
    }

    @Test
    void sameStudentsAndSeedGiveTheSameGroups() {
        List<MatchingCandidate> students = students(23, 7);

        GroupAssignment first = matcher.match(students, 2026);
        GroupAssignment second = matcher.match(new ArrayList<>(students), 2026);

        assertThat(second).isEqualTo(first);
        assertThat(first.seed()).isEqualTo(2026);
    }

    @Test
    void studentsWithoutDataArePlacedToo() {
        List<MatchingCandidate> students = new ArrayList<>(students(8, 3));
        students.add(new MatchingCandidate(99, Map.of()));

        GroupAssignment assignment = matcher.match(students, 1);

        assertThat(assignment.studentIds()).contains(99L).hasSize(9);
    }

    @Test
    void keepsTheWarningOfTheSizePlan() {
        GroupAssignment assignment = matcher.match(students(7, 1), 1);

        assertThat(assignment.sizes()).containsExactly(4, 3);
        assertThat(assignment.warning()).hasValueSatisfying(warning -> assertThat(warning).contains("7 students"));
    }

    @Test
    void seedingDealsTheStudentsInASerpentineOrder() {
        // overall 80, 70, ..., 10: group 1 gets the 1st, 4th, 5th and 8th; group 2 the others
        List<MatchingCandidate> students = new ArrayList<>();
        for (int rank = 0; rank < 8; rank++) {
            students.add(withOverall(rank + 1, 80 - 10 * rank));
        }

        List<List<MatchingCandidate>> groups = matcher.seed(students, List.of(4, 4), new Random(0));

        assertThat(groups.get(0)).extracting(MatchingCandidate::studentId).containsExactly(1L, 4L, 5L, 8L);
        assertThat(groups.get(1)).extracting(MatchingCandidate::studentId).containsExactly(2L, 3L, 6L, 7L);
    }

    @Test
    void seedingSkipsGroupsThatAreAlreadyFull() {
        List<MatchingCandidate> students = new ArrayList<>();
        for (int rank = 0; rank < 14; rank++) {
            students.add(withOverall(rank + 1, 90 - 5 * rank));
        }

        List<List<MatchingCandidate>> groups = matcher.seed(students, List.of(5, 5, 4), new Random(0));

        assertThat(groups).extracting(List::size).containsExactly(5, 5, 4);
    }

    @Test
    void rejectsAStudentListedTwice() {
        List<MatchingCandidate> students = new ArrayList<>(students(5, 1));
        students.add(withOverall(1, 50));

        assertThatThrownBy(() -> matcher.match(students, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Student 1");
    }

    @Test
    void rejectsFewerThanFourStudents() {
        assertThatThrownBy(() -> matcher.match(students(3, 1), 1))
                .isInstanceOf(NotEnoughStudentsException.class);
    }
}
