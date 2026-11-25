package pt.upt.studymatch.grouping.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

/**
 * Forms complementary groups that are balanced between each other (ADR-0004, D1).
 *
 * <ol>
 *   <li>Sizes come from the {@link GroupSizePlanner} (D2).</li>
 *   <li>Seeding: students sorted by overall effective level are dealt in a serpentine order
 *       (1→n, n→1), so every group gets strong and weaker students.</li>
 *   <li>Local search: two students of different groups are swapped whenever the score improves; it
 *       stops after a pass without improvement, at most {@value #MAX_PASSES} passes. Only the two groups
 *       involved are recomputed.</li>
 * </ol>
 *
 * <p>Ties and the order in which swaps are tried come from a {@link Random} built from the seed, so the
 * same students and seed always give the same groups. No Spring and no database: the caller loads the profiles.
 */
public class GroupMatcher {

    public static final int MAX_PASSES = 50;

    /** Smaller gains are rounding noise and would make the search loop forever. */
    private static final double MIN_GAIN = 1e-9;

    /** Students without any data are dealt last; this is only an order, never a level (R9). */
    private static final Comparator<MatchingCandidate> STRONGEST_FIRST =
            Comparator.comparingDouble((MatchingCandidate candidate) -> candidate.overall().orElse(-1)).reversed();

    private final GroupSizePlanner planner;
    private final GroupScorer scorer;

    public GroupMatcher() {
        this(new GroupSizePlanner(), new GroupScorer());
    }

    public GroupMatcher(GroupSizePlanner planner, GroupScorer scorer) {
        this.planner = Objects.requireNonNull(planner, "planner");
        this.scorer = Objects.requireNonNull(scorer, "scorer");
    }

    public GroupAssignment match(List<MatchingCandidate> candidates, long seed) {
        Objects.requireNonNull(candidates, "candidates");
        requireDistinctStudents(candidates);
        GroupPlan plan = planner.plan(candidates.size());
        Random random = new Random(seed);

        List<List<MatchingCandidate>> groups = seed(candidates, plan.sizes(), random);
        improve(groups, random);
        return toAssignment(groups, seed, plan.warning());
    }

    /** Serpentine dealing of the students, strongest first, into groups of the given sizes. */
    List<List<MatchingCandidate>> seed(List<MatchingCandidate> candidates, List<Integer> sizes, Random random) {
        List<MatchingCandidate> order = new ArrayList<>(candidates);
        Collections.shuffle(order, random);
        order.sort(STRONGEST_FIRST);

        int count = sizes.size();
        List<List<MatchingCandidate>> groups = new ArrayList<>(count);
        sizes.forEach(size -> groups.add(new ArrayList<>(size)));

        int turn = 0;
        for (MatchingCandidate candidate : order) {
            int group;
            do {
                int round = turn / count;
                int offset = turn % count;
                group = round % 2 == 0 ? offset : count - 1 - offset;
                turn++;
            } while (groups.get(group).size() == sizes.get(group));
            groups.get(group).add(candidate);
        }
        return groups;
    }

    /**
     * Swaps students between groups, in place, while the score improves.
     *
     * @return the number of passes made, at most {@link #MAX_PASSES}
     */
    int improve(List<List<MatchingCandidate>> groups, Random random) {
        int count = groups.size();
        double[] coverage = new double[count];
        double[] strength = new double[count];
        for (int g = 0; g < count; g++) {
            coverage[g] = scorer.coverage(groups.get(g));
            strength[g] = scorer.strength(groups.get(g));
        }
        double best = scorer.combine(coverage, strength);

        List<int[]> pairs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            for (int j = i + 1; j < count; j++) {
                pairs.add(new int[] {i, j});
            }
        }

        int passes = 0;
        boolean improved = true;
        while (improved && passes < MAX_PASSES) {
            improved = false;
            passes++;
            Collections.shuffle(pairs, random);
            for (int[] pair : pairs) {
                int i = pair[0];
                int j = pair[1];
                List<MatchingCandidate> first = groups.get(i);
                List<MatchingCandidate> second = groups.get(j);
                for (int a = 0; a < first.size(); a++) {
                    for (int b = 0; b < second.size(); b++) {
                        double[] before = {coverage[i], strength[i], coverage[j], strength[j]};
                        swap(first, a, second, b);
                        coverage[i] = scorer.coverage(first);
                        strength[i] = scorer.strength(first);
                        coverage[j] = scorer.coverage(second);
                        strength[j] = scorer.strength(second);

                        double score = scorer.combine(coverage, strength);
                        if (score > best + MIN_GAIN) {
                            best = score;
                            improved = true;
                        } else {
                            swap(first, a, second, b);
                            coverage[i] = before[0];
                            strength[i] = before[1];
                            coverage[j] = before[2];
                            strength[j] = before[3];
                        }
                    }
                }
            }
        }
        return passes;
    }

    private static void swap(List<MatchingCandidate> first, int a, List<MatchingCandidate> second, int b) {
        MatchingCandidate moved = first.get(a);
        first.set(a, second.get(b));
        second.set(b, moved);
    }

    GroupAssignment toAssignment(List<List<MatchingCandidate>> groups, long seed, Optional<String> warning) {
        List<MatchedGroup> matched = new ArrayList<>(groups.size());
        for (int i = 0; i < groups.size(); i++) {
            List<MatchingCandidate> members = groups.get(i);
            List<Long> ids = members.stream().map(MatchingCandidate::studentId).toList();
            matched.add(new MatchedGroup(i + 1, ids, scorer.coverage(members)));
        }
        return new GroupAssignment(matched, scorer.score(groups), seed, warning);
    }

    private static void requireDistinctStudents(List<MatchingCandidate> candidates) {
        Set<Long> seen = new HashSet<>();
        for (MatchingCandidate candidate : candidates) {
            Objects.requireNonNull(candidate, "candidate");
            if (!seen.add(candidate.studentId())) {
                throw new IllegalArgumentException("Student " + candidate.studentId() + " appears more than once");
            }
        }
    }
}
