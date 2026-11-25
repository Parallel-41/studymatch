package pt.upt.studymatch.grouping.domain;

import java.util.List;

/** One generated group: its number (from 1), its members and its coverage on the 0–100 scale. */
public record MatchedGroup(int number, List<Long> studentIds, double coverage) {

    public MatchedGroup {
        if (number < 1) {
            throw new IllegalArgumentException("Group numbers start at 1, got " + number);
        }
        if (studentIds == null || studentIds.isEmpty()) {
            throw new IllegalArgumentException("A group needs at least one student");
        }
        studentIds = List.copyOf(studentIds);
    }

    public int size() {
        return studentIds.size();
    }
}
