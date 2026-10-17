package pt.upt.studymatch.academic.domain;

import org.junit.jupiter.api.Test;
import pt.upt.studymatch.common.domain.Grade;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EnrolmentTest {

    @Test
    void returnsEmptyWhenThereAreNoAttempts() {
        Enrolment enrolment = new Enrolment(
            null, null, "2026/2027", EnrolmentStatus.ENROLLED
        );

        assertTrue(enrolment.bestPassingGrade().isEmpty());
    }

    @Test
    void returnsEmptyWhenNoAttemptHasPassingGrade() {
        Enrolment enrolment = new Enrolment(
            null, null, "2026/2027", EnrolmentStatus.ENROLLED
        );

        enrolment.addAttempt(new Attempt(
            enrolment, AssessmentSeason.NORMAL,
            new Grade(8), LocalDate.of(2027, 1, 15)
        ));
        enrolment.addAttempt(new Attempt(
            enrolment, AssessmentSeason.RECURSO,
            new Grade(9), LocalDate.of(2027, 2, 15)
        ));

        assertTrue(enrolment.bestPassingGrade().isEmpty());
    }

    @Test
    void considersGradeTenAsPassing() {
        Enrolment enrolment = new Enrolment(
            null, null, "2026/2027", EnrolmentStatus.ENROLLED
        );

        enrolment.addAttempt(new Attempt(
            enrolment, AssessmentSeason.NORMAL,
            new Grade(10), LocalDate.of(2027, 1, 15)
        ));

        assertEquals(
            new Grade(10),
            enrolment.bestPassingGrade().orElseThrow()
        );
    }

    @Test
    void returnsHighestPassingGradeAcrossAllAttempts() {
        Enrolment enrolment = new Enrolment(
            null, null, "2026/2027", EnrolmentStatus.ENROLLED
        );

        enrolment.addAttempt(new Attempt(
            enrolment, AssessmentSeason.NORMAL,
            new Grade(12), LocalDate.of(2027, 1, 15)
        ));
        enrolment.addAttempt(new Attempt(
            enrolment, AssessmentSeason.RECURSO,
            new Grade(9), LocalDate.of(2027, 2, 15)
        ));
        enrolment.addAttempt(new Attempt(
            enrolment, AssessmentSeason.MELHORIA,
            new Grade(18), LocalDate.of(2027, 3, 15)
        ));

        assertEquals(
            new Grade(18),
            enrolment.bestPassingGrade().orElseThrow()
        );
    }
}
