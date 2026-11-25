package pt.upt.studymatch.grouping.domain;

import java.util.List;
import java.util.Optional;

/**
 * Result of one run of the {@link GroupMatcher}: the groups, the overall score (ADR-0004, D1), the seed
 * that reproduces it and the warning of the size plan, if any (D2).
 */
public record GroupAssignment(List<MatchedGroup> groups, double score, long seed, Optional<String> warning) {

    public GroupAssignment {
        if (groups == null || groups.isEmpty()) {
            throw new IllegalArgumentException("An assignment needs at least one group");
        }
        groups = List.copyOf(groups);
        warning = warning == null ? Optional.empty() : warning;
    }

    public List<Long> studentIds() {
        return groups.stream().flatMap(group -> group.studentIds().stream()).toList();
    }

    public List<Integer> sizes() {
        return groups.stream().map(MatchedGroup::size).toList();
    }
}
