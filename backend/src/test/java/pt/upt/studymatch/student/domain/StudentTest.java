package pt.upt.studymatch.student.domain;

import org.junit.jupiter.api.Test;
import pt.upt.studymatch.academic.domain.DegreeProgram;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StudentTest {

    private Clock fixedClock() {
        return Clock.fixed(
            Instant.parse("2026-10-17T12:00:00Z"),
            ZoneOffset.UTC
        );
    }

    private Student createStudent(int registrationYear) {
        DegreeProgram program = new DegreeProgram(
            "LEI",
            "Licenciatura em Engenharia Informática"
        );

        return new Student(
            "2024001",
            "João Silva",
            "joao.silva@example.com",
            registrationYear,
            program
        );
    }

    @Test
    void registrationInCurrentYearMeansFirstYear() {
        Student student = createStudent(2026);

        assertEquals(1, student.currentYear(fixedClock()));
    }

    @Test
    void registrationLastYearMeansSecondYear() {
        Student student = createStudent(2025);

        assertEquals(2, student.currentYear(fixedClock()));
    }

    @Test
    void registrationThreeYearsAgoMeansFourthYear() {
        Student student = createStudent(2023);

        assertEquals(4, student.currentYear(fixedClock()));
    }

    @Test
    void calculationUsesYearFromProvidedClock() {
        Student student = createStudent(2025);

        Clock clockFor2027 = Clock.fixed(
            Instant.parse("2027-03-01T12:00:00Z"),
            ZoneOffset.UTC
        );

        assertEquals(2, student.currentYear(clockFor2027));
    }

    @Test
    void newAcademicYearStartsInSeptember() {
        Student student = createStudent(2025);
        Clock aug2026 = Clock.fixed(
            Instant.parse("2026-08-31T12:00:00Z"), ZoneOffset.UTC);
        Clock sep2026 = Clock.fixed(
            Instant.parse("2026-09-01T12:00:00Z"), ZoneOffset.UTC);

        assertEquals(1, student.currentYear(aug2026));
        assertEquals(2, student.currentYear(sep2026));
    }
}
