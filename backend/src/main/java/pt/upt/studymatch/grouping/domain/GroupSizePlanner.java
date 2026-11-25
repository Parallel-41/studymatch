package pt.upt.studymatch.grouping.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Splits n eligible students into groups of 4 or 5 whenever that is possible (brief §3.1, ADR-0004 D2).
 *
 * <p>The number of groups is the smallest that fits ({@code ⌈n / 5⌉}) and the sizes are as even as
 * possible, larger groups first: 23 → 5+5+5+4+4. For 6, 7 and 11 no split into 4s and 5s exists; the
 * closest split (3+3, 4+3, 4+4+3) is returned with a warning. Fewer than 4 students is rejected.
 */
public class GroupSizePlanner {

    public static final int MIN_GROUP_SIZE = 4;
    public static final int MAX_GROUP_SIZE = 5;

    public GroupPlan plan(int students) {
        if (students < 0) {
            throw new IllegalArgumentException("The number of students cannot be negative, got " + students);
        }
        if (students < MIN_GROUP_SIZE) {
            throw new NotEnoughStudentsException(students, MIN_GROUP_SIZE);
        }

        int groups = (students + MAX_GROUP_SIZE - 1) / MAX_GROUP_SIZE;
        int base = students / groups;
        int larger = students % groups;

        List<Integer> sizes = new ArrayList<>(groups);
        for (int i = 0; i < groups; i++) {
            sizes.add(i < larger ? base + 1 : base);
        }

        Optional<String> warning = base < MIN_GROUP_SIZE
                ? Optional.of(warningFor(students, sizes))
                : Optional.empty();
        return new GroupPlan(sizes, warning);
    }

    private static String warningFor(int students, List<Integer> sizes) {
        String split = sizes.stream().map(String::valueOf).collect(Collectors.joining("+"));
        return "No split into groups of 4 or 5 exists for " + students
                + " students; using the closest split " + split + ".";
    }
}
