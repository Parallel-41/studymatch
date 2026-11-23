package pt.upt.studymatch.grouping.domain;

import java.util.List;
import java.util.Optional;

/**
 * Sizes of the groups for one generation, plus a warning when no split into
 * groups of 4 or 5 exists (ADR-0004, D2).
 */
public record GroupPlan(List<Integer> sizes, Optional<String> warning) {

    public GroupPlan {
        if (sizes == null || sizes.isEmpty()) {
            throw new IllegalArgumentException("A plan needs at least one group");
        }
        sizes = List.copyOf(sizes);
        warning = warning == null ? Optional.empty() : warning;
    }

    public int groupCount() {
        return sizes.size();
    }

    public int studentCount() {
        return sizes.stream().mapToInt(Integer::intValue).sum();
    }

    public boolean hasWarning() {
        return warning.isPresent();
    }
}
