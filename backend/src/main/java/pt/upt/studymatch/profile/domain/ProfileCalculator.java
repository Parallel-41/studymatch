package pt.upt.studymatch.profile.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import pt.upt.studymatch.academic.domain.EvidenceGrade;
import pt.upt.studymatch.common.domain.Confidence;
import pt.upt.studymatch.common.domain.Grade;
import pt.upt.studymatch.common.domain.Level;
import pt.upt.studymatch.competency.domain.Competency;
import pt.upt.studymatch.competency.domain.CourseUnitWeight;

/**
 * Turns a student's evidence (best passing grades), the course unit weights and the
 * self-assessment into a competency profile. Pure: no Spring, no database, no clock.
 *
 * <p>Formulas from ADR-0003:
 * <ul>
 *   <li>F1: {@code score(g) = 50 + (g - 10) * 5}, passing grades only</li>
 *   <li>F2: failed grades give no evidence</li>
 *   <li>F3: {@code r = 0.85^semestersAgo}, {@code w = ects * weight * r},
 *       {@code evidenceLevel = sum(w * score) / sum(w)}, {@code evidenceEcts = sum(w)}</li>
 *   <li>F4: blending with the self-assessment is done by {@link ColdStartPolicy}</li>
 * </ul>
 */
public class ProfileCalculator {

    public static final double RECENCY_FACTOR = 0.85;

    private final ColdStartPolicy coldStartPolicy;

    public ProfileCalculator() {
        this(new ColdStartPolicy());
    }

    public ProfileCalculator(ColdStartPolicy coldStartPolicy) {
        this.coldStartPolicy = Objects.requireNonNull(coldStartPolicy, "coldStartPolicy");
    }

    /**
     * @param evidence             best passing grade per course unit (from AcademicHistoryService)
     * @param weights              course unit to competency weights (from CompetencyCatalogueService)
     * @param selfAssessmentByArea area code to self-assessed level (empty map if none)
     * @param competencies         competencies of the student's degree programme, in display order;
     *                             only their areas appear in the profile
     */
    public StudentProfile calculate(List<EvidenceGrade> evidence,
                                    List<CourseUnitWeight> weights,
                                    Map<String, Level> selfAssessmentByArea,
                                    List<Competency> competencies) {
        Objects.requireNonNull(evidence, "evidence");
        Objects.requireNonNull(weights, "weights");
        Objects.requireNonNull(selfAssessmentByArea, "selfAssessmentByArea");
        Objects.requireNonNull(competencies, "competencies");

        Map<String, EvidenceGrade> passedUnits = passedUnits(evidence);
        List<CompetencyLevel> competencyLevels = new ArrayList<>();
        Map<String, List<CompetencyLevel>> levelsByArea = new LinkedHashMap<>();

        for (Competency competency : competencies) {
            String areaCode = competency.getArea().getCode();
            List<Contribution> contributions = contributionsFor(competency.getCode(), passedUnits, weights);
            CompetencyLevel level = blend(competency.getCode(), contributions,
                Optional.ofNullable(selfAssessmentByArea.get(areaCode)));
            competencyLevels.add(level);
            levelsByArea.computeIfAbsent(areaCode, k -> new ArrayList<>()).add(level);
        }

        List<AreaLevel> areaLevels = levelsByArea.entrySet().stream()
            .map(entry -> areaLevel(entry.getKey(), entry.getValue()))
            .toList();

        return new StudentProfile(areaLevels, competencyLevels);
    }

    /**
     * #56: explains one competency with the same formulas as {@link #calculate}, so the explanation
     * always matches the profile.
     */
    public CompetencyExplanation explain(String competencyCode,
                                         List<EvidenceGrade> evidence,
                                         List<CourseUnitWeight> weights,
                                         Optional<Level> selfAssessed) {
        Objects.requireNonNull(competencyCode, "competencyCode");
        List<Contribution> contributions = contributionsFor(competencyCode, passedUnits(evidence), weights);
        CompetencyLevel level = blend(competencyCode, contributions, selfAssessed);

        double selfAssessedShare;
        if (!level.hasData() || selfAssessed.isEmpty()) {
            selfAssessedShare = 0;
        } else if (contributions.isEmpty()) {
            selfAssessedShare = 1;
        } else {
            selfAssessedShare = 1 - level.confidence().value();
        }
        return new CompetencyExplanation(level, contributions, selfAssessedShare);
    }

    /** F1: value of a passing grade on the 0–100 scale (10 → 50, 20 → 100). */
    public static double score(Grade grade) {
        if (!grade.isPassing()) {
            throw new IllegalArgumentException("Only passing grades have a score, got " + grade.value());
        }
        return 50 + (grade.value() - Grade.PASS_MARK) * 5.0;
    }

    /** F3: weight of a grade obtained {@code semestersAgo} semesters ago (1 for the current one). */
    public static double recency(int semestersAgo) {
        if (semestersAgo < 0) {
            throw new IllegalArgumentException("semestersAgo cannot be negative");
        }
        return Math.pow(RECENCY_FACTOR, semestersAgo);
    }

    private static Map<String, EvidenceGrade> passedUnits(List<EvidenceGrade> evidence) {
        Objects.requireNonNull(evidence, "evidence");
        return evidence.stream()
            .filter(e -> e.grade().isPassing())
            .collect(Collectors.toMap(EvidenceGrade::courseUnitCode, Function.identity(),
                (a, b) -> a.grade().value() >= b.grade().value() ? a : b, LinkedHashMap::new));
    }

    /** F3: one contribution per passed course unit that has a weight for the competency. */
    private static List<Contribution> contributionsFor(String competencyCode,
                                                      Map<String, EvidenceGrade> passedUnits,
                                                      List<CourseUnitWeight> weights) {
        Objects.requireNonNull(weights, "weights");
        record Part(EvidenceGrade unit, CourseUnitWeight weight, double recency, double effectiveWeight, double score) {
        }
        List<Part> parts = new ArrayList<>();
        for (CourseUnitWeight weight : weights) {
            EvidenceGrade unit = passedUnits.get(weight.courseUnitCode());
            if (!weight.competencyCode().equals(competencyCode) || unit == null) {
                continue;
            }
            double r = recency(unit.semestersAgo());
            parts.add(new Part(unit, weight, r, unit.ects() * weight.weight() * r, score(unit.grade())));
        }

        double totalWeight = parts.stream().mapToDouble(Part::effectiveWeight).sum();
        double totalWeightedScore = parts.stream().mapToDouble(p -> p.effectiveWeight() * p.score()).sum();

        return parts.stream()
            .map(p -> new Contribution(p.unit().courseUnitCode(), p.unit().ects(), p.unit().grade().value(),
                p.weight().weight(), p.recency(), p.effectiveWeight(),
                p.effectiveWeight() * p.score() / totalWeight,
                p.effectiveWeight() * p.score() / totalWeightedScore))
            .toList();
    }

    /** Evidence level = sum of the points; evidenceEcts = sum of the effective weights; F4 in ColdStartPolicy. */
    private CompetencyLevel blend(String competencyCode, List<Contribution> contributions,
                                  Optional<Level> selfAssessed) {
        double evidenceEcts = contributions.stream().mapToDouble(Contribution::effectiveWeight).sum();
        Optional<Level> evidenceLevel = contributions.isEmpty()
            ? Optional.empty()
            : Optional.of(level(contributions.stream().mapToDouble(Contribution::points).sum()));
        return coldStartPolicy.blend(competencyCode, evidenceLevel, evidenceEcts, selfAssessed);
    }

    private static AreaLevel areaLevel(String areaCode, List<CompetencyLevel> levels) {
        List<CompetencyLevel> withData = levels.stream().filter(CompetencyLevel::hasData).toList();
        if (withData.isEmpty()) {
            return AreaLevel.noData(areaCode);
        }
        double meanLevel = withData.stream().mapToDouble(c -> c.level().value()).average().orElseThrow();
        double meanConfidence = withData.stream().mapToDouble(c -> c.confidence().value()).average().orElseThrow();
        return new AreaLevel(areaCode, level(meanLevel), new Confidence(Math.min(1.0, meanConfidence)));
    }

    /** Guards against floating-point results like 100.00000000000001. */
    private static Level level(double value) {
        return new Level(Math.max(0.0, Math.min(100.0, value)));
    }
}
