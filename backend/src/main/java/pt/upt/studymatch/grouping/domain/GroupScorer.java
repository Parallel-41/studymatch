package pt.upt.studymatch.grouping.domain;

import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;

/**
 * Scores a set of groups (ADR-0004, D1): complementary inside each group, balanced between groups.
 *
 * <ul>
 *   <li>{@code coverage(g)}: mean over the areas of the best effective level in the group; an area where
 *       no member has data gives no coverage.</li>
 *   <li>{@code strength(g)}: mean overall effective level of the members with data.</li>
 *   <li>{@code spread}: standard deviation of the groups' strength.</li>
 *   <li>{@code score = mean coverage − 0.5 × spread}, on the 0–100 scale.</li>
 * </ul>
 */
public class GroupScorer {

    public static final List<String> DEFAULT_AREAS = List.of("PROG", "DB", "WEB", "QUAL", "MATH", "SOFT");
    public static final double SPREAD_WEIGHT = 0.5;

    private final List<String> areaCodes;

    public GroupScorer() {
        this(DEFAULT_AREAS);
    }

    public GroupScorer(List<String> areaCodes) {
        Objects.requireNonNull(areaCodes, "areaCodes");
        if (areaCodes.isEmpty()) {
            throw new IllegalArgumentException("At least one competency area is needed");
        }
        this.areaCodes = List.copyOf(areaCodes);
    }

    public List<String> areaCodes() {
        return areaCodes;
    }

    public double coverage(List<MatchingCandidate> group) {
        double total = 0;
        for (String area : areaCodes) {
            total += group.stream()
                    .map(member -> member.effectiveLevel(area))
                    .filter(OptionalDouble::isPresent)
                    .mapToDouble(OptionalDouble::getAsDouble)
                    .max()
                    .orElse(0);
        }
        return total / areaCodes.size();
    }

    public double strength(List<MatchingCandidate> group) {
        return group.stream()
                .map(MatchingCandidate::overall)
                .filter(OptionalDouble::isPresent)
                .mapToDouble(OptionalDouble::getAsDouble)
                .average()
                .orElse(0);
    }

    public double score(List<List<MatchingCandidate>> groups) {
        if (groups.isEmpty()) {
            throw new IllegalArgumentException("At least one group is needed");
        }
        double[] coverage = groups.stream().mapToDouble(this::coverage).toArray();
        double[] strength = groups.stream().mapToDouble(this::strength).toArray();
        return combine(coverage, strength);
    }

    /** Combines per-group values already computed, so a swap only recomputes the two groups involved. */
    double combine(double[] coverage, double[] strength) {
        return mean(coverage) - SPREAD_WEIGHT * standardDeviation(strength);
    }

    /** Rounds to one decimal, as shown in the API. */
    public static double round(double value) {
        return Math.round(value * 10) / 10.0;
    }

    private static double mean(double[] values) {
        double sum = 0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.length;
    }

    private static double standardDeviation(double[] values) {
        double mean = mean(values);
        double squares = 0;
        for (double value : values) {
            squares += (value - mean) * (value - mean);
        }
        return Math.sqrt(squares / values.length);
    }
}
