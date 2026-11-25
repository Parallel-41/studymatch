package pt.upt.studymatch.grouping.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class GroupSizePlannerTest {

    private final GroupSizePlanner planner = new GroupSizePlanner();

    private static List<Integer> sizes(String split) {
        return Arrays.stream(split.split("\\+")).map(Integer::valueOf).toList();
    }

    @ParameterizedTest(name = "{0} students -> {1}")
    @CsvSource({
            // one group: lower and upper boundary of a group
            "4, 4",
            "5, 5",
            // two groups
            "8, 4+4",
            "9, 5+4",
            "10, 5+5",
            // three groups: from 12 on a split into 4s and 5s always exists
            "12, 4+4+4",
            "13, 5+4+4",
            "15, 5+5+5",
            "16, 4+4+4+4",
            // the brief's example (§3.1)
            "23, 5+5+5+4+4",
            "100, 5+5+5+5+5+5+5+5+5+5+5+5+5+5+5+5+5+5+5+5"
    })
    void splitsIntoGroupsOfFourOrFive(int students, String expected) {
        GroupPlan plan = planner.plan(students);

        assertThat(plan.sizes()).isEqualTo(sizes(expected));
        assertThat(plan.hasWarning()).isFalse();
    }

    @ParameterizedTest(name = "{0} students -> closest split {1} with a warning")
    @CsvSource({
            "6, 3+3",
            "7, 4+3",
            "11, 4+4+3"
    })
    void usesTheClosestSplitWithAWarningWhenNoFourOrFiveSplitExists(int students, String expected) {
        GroupPlan plan = planner.plan(students);

        assertThat(plan.sizes()).isEqualTo(sizes(expected));
        assertThat(plan.warning()).hasValueSatisfying(warning -> assertThat(warning)
                .contains(String.valueOf(students))
                .contains(expected));
    }

    @ParameterizedTest(name = "{0} students are not enough")
    @ValueSource(ints = {0, 1, 3})
    void rejectsFewerStudentsThanTheSmallestGroup(int students) {
        assertThatThrownBy(() -> planner.plan(students))
                .isInstanceOf(NotEnoughStudentsException.class)
                .hasMessageContaining("At least 4 students");
    }

    @Test
    void rejectsANegativeNumberOfStudents() {
        assertThatThrownBy(() -> planner.plan(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void everyStudentIsPlacedOnceAndSizesDifferByAtMostOne() {
        IntStream.rangeClosed(4, 300).forEach(students -> {
            GroupPlan plan = planner.plan(students);
            int smallest = plan.sizes().stream().mapToInt(Integer::intValue).min().orElseThrow();
            int largest = plan.sizes().stream().mapToInt(Integer::intValue).max().orElseThrow();

            assertThat(plan.studentCount()).as("students placed for n=%d", students).isEqualTo(students);
            assertThat(largest - smallest).as("size spread for n=%d", students).isLessThanOrEqualTo(1);
            assertThat(plan.groupCount()).as("groups for n=%d", students).isEqualTo((students + 4) / 5);
        });
    }

    @Test
    void onlySixSevenAndElevenNeedAWarning() {
        List<Integer> withWarning = IntStream.rangeClosed(4, 300)
                .filter(students -> planner.plan(students).hasWarning())
                .boxed()
                .toList();

        assertThat(withWarning).containsExactly(6, 7, 11);
    }

    @Test
    void groupsWithoutAWarningAlwaysHaveFourOrFiveStudents() {
        IntStream.rangeClosed(4, 300)
                .mapToObj(planner::plan)
                .filter(plan -> !plan.hasWarning())
                .forEach(plan -> assertThat(plan.sizes()).allMatch(size -> size == 4 || size == 5));
    }
}
