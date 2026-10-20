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

        Map<String, EvidenceGrade> passedUnits = evidence.stream()
            .filter(e -> e.grade().isPassing())
            .collect(Collectors.toMap(EvidenceGrade::courseUnitCode, Function.identity(),
                (a, b) -> a.grade().value() >= b.grade().value() ? a : b));

        List<CompetencyLevel> competencyLevels = new ArrayList<>();
        Map<String, List<CompetencyLevel>> levelsByArea = new LinkedHashMap<>();

        for (Competency competency : competencies) {
            String areaCode = competency.getArea().getCode();
            CompetencyLevel level = levelFor(competency.getCode(), passedUnits, weights,
                Optional.ofNullable(selfAssessmentByArea.get(areaCode)));
            competencyLevels.add(level);
            levelsByArea.computeIfAbsent(areaCode, k -> new ArrayList<>()).add(level);
        }

        List<AreaLevel> areaLevels = levelsByArea.entrySet().stream()
            .map(entry -> areaLevel(entry.getKey(), entry.getValue()))
            .toList();

        return new StudentProfile(areaLevels, competencyLevels);
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

    private CompetencyLevel levelFor(String competencyCode,
                                     Map<String, EvidenceGrade> passedUnits,
                                     List<CourseUnitWeight> weights,
                                     Optional<Level> selfAssessed) {
        double weightedScores = 0;
        double evidenceEcts = 0;

        for (CourseUnitWeight weight : weights) {
            if (!weight.competencyCode().equals(competencyCode)) {
                continue;
            }
            EvidenceGrade unit = passedUnits.get(weight.courseUnitCode());
            if (unit == null) {
                continue;
            }
            double w = unit.ects() * weight.weight() * recency(unit.semestersAgo());
            weightedScores += w * score(unit.grade());
            evidenceEcts += w;
        }

        Optional<Level> evidenceLevel = evidenceEcts > 0
            ? Optional.of(level(weightedScores / evidenceEcts))
            : Optional.empty();

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
