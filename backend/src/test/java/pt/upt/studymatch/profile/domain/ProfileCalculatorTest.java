package pt.upt.studymatch.profile.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import pt.upt.studymatch.academic.domain.EvidenceGrade;
import pt.upt.studymatch.common.domain.Grade;
import pt.upt.studymatch.common.domain.Level;
import pt.upt.studymatch.competency.domain.Competency;
import pt.upt.studymatch.competency.domain.CompetencyArea;
import pt.upt.studymatch.competency.domain.CourseUnitWeight;

class ProfileCalculatorTest {

    private static final CompetencyArea PROG = new CompetencyArea("PROG", "Programming");
    private static final CompetencyArea DB = new CompetencyArea("DB", "Databases");
    private static final Competency OOP = new Competency("PROG_OO", "Object-Oriented Programming", PROG);
    private static final Competency ALG = new Competency("PROG_ALG", "Algorithms & Data Structures", PROG);
    private static final Competency SQL = new Competency("DB_SQL", "Relational Databases & SQL", DB);
    private static final List<Competency> CATALOGUE = List.of(OOP, ALG, SQL);

    private final ProfileCalculator calculator = new ProfileCalculator();

    private static EvidenceGrade evidence(String unit, int ects, int grade, int semestersAgo) {
        return new EvidenceGrade(unit, ects, new Grade(grade), semestersAgo);
    }

    // F1 — value of a passing grade, boundaries 10 and 20
    @ParameterizedTest(name = "grade {0} -> score {1}")
    @CsvSource({"10, 50", "12, 60", "14, 70", "15, 75", "18, 90", "20, 100"})
    void scoreMapsPassingGradesLinearlyFrom50To100(int grade, double expectedScore) {
        assertThat(ProfileCalculator.score(new Grade(grade))).isEqualTo(expectedScore);
    }

    @Test
    void scoreRejectsFailingGrade() {
        assertThatThrownBy(() -> ProfileCalculator.score(new Grade(9)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // F3 — recency
    @ParameterizedTest(name = "{0} semesters ago -> {1}")
    @CsvSource({"0, 1.0", "1, 0.85", "2, 0.7225", "4, 0.52200625"})
    void recencyDecaysBy085PerSemester(int semestersAgo, double expected) {
        assertThat(ProfileCalculator.recency(semestersAgo)).isCloseTo(expected, within(1e-9));
    }

    @Test
    void emptyHistoryAndNoSelfAssessmentMeansNoDataNotZero() {
        StudentProfile profile = calculator.calculate(List.of(), List.of(), Map.of(), CATALOGUE);

        assertThat(profile.competencies()).allSatisfy(c -> {
            assertThat(c.hasData()).isFalse();
            assertThat(c.level()).isNull();
        });
        assertThat(profile.areas()).allSatisfy(a -> assertThat(a.level()).isNull());
    }

    @Test
    void singleCourseUnitGivesItsScoreAndConfidenceFromItsEcts() {
        StudentProfile profile = calculator.calculate(
            List.of(evidence("PROG1", 6, 14, 0)),
            List.of(new CourseUnitWeight("PROG1", "PROG_OO", 1.0)),
            Map.of(),
            CATALOGUE);

        CompetencyLevel oop = profile.competency("PROG_OO").orElseThrow();
        assertThat(oop.level().value()).isCloseTo(70.0, within(1e-9));
        assertThat(oop.evidenceLevel().value()).isCloseTo(70.0, within(1e-9));
        assertThat(oop.confidence().value()).isCloseTo(0.5, within(1e-9)); // 6 / 12 ECTS
    }

    @Test
    void weightedAverageUsesEctsTimesWeight() {
        // A: 6 ECTS × 1.0, grade 12 → 60 ; B: 6 ECTS × 0.5, grade 18 → 90
        StudentProfile profile = calculator.calculate(
            List.of(evidence("A", 6, 12, 0), evidence("B", 6, 18, 0)),
            List.of(new CourseUnitWeight("A", "PROG_OO", 1.0), new CourseUnitWeight("B", "PROG_OO", 0.5)),
            Map.of(),
            CATALOGUE);

        // (6×60 + 3×90) / 9 = 70
        assertThat(profile.competency("PROG_OO").orElseThrow().evidenceLevel().value())
            .isCloseTo(70.0, within(1e-9));
    }

    @Test
    void olderGradesCountLessThanRecentOnes() {
        List<CourseUnitWeight> weights = List.of(
            new CourseUnitWeight("A", "PROG_OO", 1.0), new CourseUnitWeight("B", "PROG_OO", 1.0));

        double recentHigh = calculator.calculate(
                List.of(evidence("A", 6, 20, 0), evidence("B", 6, 10, 4)), weights, Map.of(), CATALOGUE)
            .competency("PROG_OO").orElseThrow().evidenceLevel().value();
        double oldHigh = calculator.calculate(
                List.of(evidence("A", 6, 20, 4), evidence("B", 6, 10, 0)), weights, Map.of(), CATALOGUE)
            .competency("PROG_OO").orElseThrow().evidenceLevel().value();

        assertThat(recentHigh).isGreaterThan(75.0).isGreaterThan(oldHigh);
        assertThat(oldHigh).isLessThan(75.0);
    }

    @Test
    void oldOnlyEvidenceLowersConfidence() {
        List<CourseUnitWeight> weights = List.of(new CourseUnitWeight("A", "PROG_OO", 1.0));

        double recent = calculator.calculate(List.of(evidence("A", 6, 15, 0)), weights, Map.of(), CATALOGUE)
            .competency("PROG_OO").orElseThrow().confidence().value();
        double old = calculator.calculate(List.of(evidence("A", 6, 15, 4)), weights, Map.of(), CATALOGUE)
            .competency("PROG_OO").orElseThrow().confidence().value();

        assertThat(old).isLessThan(recent);
    }

    @Test
    void failedGradesAndUnitsWithoutWeightGiveNoEvidence() {
        StudentProfile profile = calculator.calculate(
            List.of(evidence("A", 6, 9, 0), evidence("C", 6, 18, 0)),
            List.of(new CourseUnitWeight("A", "PROG_OO", 1.0)),
            Map.of(),
            CATALOGUE);

        assertThat(profile.competency("PROG_OO").orElseThrow().hasData()).isFalse();
    }

    @Test
    void levelNeverExceeds100() {
        StudentProfile profile = calculator.calculate(
            List.of(evidence("A", 6, 20, 0), evidence("B", 5, 20, 3), evidence("C", 7, 20, 1)),
            List.of(new CourseUnitWeight("A", "PROG_OO", 0.3),
                new CourseUnitWeight("B", "PROG_OO", 0.7),
                new CourseUnitWeight("C", "PROG_OO", 0.9)),
            Map.of(),
            CATALOGUE);

        assertThat(profile.competency("PROG_OO").orElseThrow().level().value()).isLessThanOrEqualTo(100.0);
    }

    @Test
    void selfAssessmentOnlyGivesLevelWithZeroConfidence() {
        StudentProfile profile = calculator.calculate(List.of(), List.of(), Map.of("PROG", new Level(40)), CATALOGUE);

        CompetencyLevel oop = profile.competency("PROG_OO").orElseThrow();
        assertThat(oop.level().value()).isEqualTo(40.0);
        assertThat(oop.confidence().value()).isZero();
        assertThat(profile.competency("DB_SQL").orElseThrow().hasData()).isFalse();
    }

    @Test
    void areaAveragesOnlyCompetenciesWithData() {
        StudentProfile profile = calculator.calculate(
            List.of(evidence("A", 12, 16, 0)),
            List.of(new CourseUnitWeight("A", "PROG_OO", 1.0)),
            Map.of(),
            CATALOGUE);

        AreaLevel prog = profile.area("PROG").orElseThrow();
        assertThat(prog.level().value()).isCloseTo(80.0, within(1e-9)); // PROG_ALG has no data, ignored
        assertThat(prog.confidence().value()).isCloseTo(1.0, within(1e-9));
        assertThat(profile.area("DB").orElseThrow().hasData()).isFalse();
    }

    // ADR-0003, section 3 — worked example
    @Test
    void matchesWorkedExampleOfAdr0003() {
        StudentProfile profile = calculator.calculate(
            List.of(evidence("A", 6, 12, 2), evidence("B", 6, 15, 0)),
            List.of(new CourseUnitWeight("A", "PROG_OO", 1.0), new CourseUnitWeight("B", "PROG_OO", 0.5)),
            Map.of("PROG", new Level(50)),
            CATALOGUE);

        CompetencyLevel oop = profile.competency("PROG_OO").orElseThrow();
        assertThat(oop.evidenceLevel().value()).isCloseTo(66.1, within(0.05));
        assertThat(oop.confidence().value()).isCloseTo(0.611, within(0.001));
        assertThat(oop.level().value()).isCloseTo(59.9, within(0.05));
        assertThat(oop.selfAssessedLevel().value()).isEqualTo(50.0);
    }
}
