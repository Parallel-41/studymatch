
package pt.upt.studymatch.academic.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import pt.upt.studymatch.student.domain.Student;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnrolmentTest {

    private Enrolment createEnrolment() {
        DegreeProgram degreeProgram =
            new DegreeProgram("LEI", "Licenciatura em Informática");

        Student student = new Student(
            "2024001",
            "Aluno Teste",
            "aluno@example.com",
            2024,
            degreeProgram
        );

        CourseUnit courseUnit = new CourseUnit(
            "POO",
            "Programação Orientada a Objetos",
            1,
            1,
            6,
            degreeProgram
        );

        return new Enrolment(
            student,
            courseUnit,
            "2024/2025",
            EnrolmentStatus.ENROLLED
        );
    }

    private void addAttempt(
        Enrolment enrolment,
        String grade,
        AssessmentSeason season) {

        Attempt attempt = new Attempt(
            enrolment,
            season,
            grade == null ? null : new BigDecimal(grade),
            LocalDate.of(2025, 1, 15)
        );

        enrolment.addAttempt(attempt);
    }

    @ParameterizedTest
    @CsvSource({
        "9, false, 0",
        "10, true, 10",
        "20, true, 20"
    })
    void bestPassingGradeHandlesBoundaries(
        String grade,
        boolean expectedPresent,
        int expectedGrade) {

        Enrolment enrolment = createEnrolment();
        addAttempt(enrolment, grade, AssessmentSeason.NORMAL);

        var result = enrolment.bestPassingGrade();

        assertEquals(expectedPresent, result.isPresent());

        if (expectedPresent) {
            assertEquals(
                0,
                result.get().compareTo(
                    BigDecimal.valueOf(expectedGrade)
                )
            );
        }
    }

    @Test
    void bestPassingGradeIsEmptyWhenNoAttemptPasses() {
        Enrolment enrolment = createEnrolment();

        addAttempt(enrolment, "8", AssessmentSeason.NORMAL);
        addAttempt(enrolment, "9", AssessmentSeason.RECURSO);

        assertTrue(enrolment.bestPassingGrade().isEmpty());
    }

    @Test
    void bestPassingGradeReturnsHighestPassingAttemptAcrossSeasons() {
        Enrolment enrolment = createEnrolment();

        addAttempt(enrolment, "16", AssessmentSeason.NORMAL);
        addAttempt(enrolment, "12", AssessmentSeason.RECURSO);
        addAttempt(enrolment, "14", AssessmentSeason.MELHORIA);
        addAttempt(enrolment, "9", AssessmentSeason.NORMAL);
        addAttempt(enrolment, "18", AssessmentSeason.MELHORIA);

        assertEquals(
            0,
            enrolment.bestPassingGrade()
                .orElseThrow()
                .compareTo(BigDecimal.valueOf(18))
        );
    }

}
