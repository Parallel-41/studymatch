
package pt.upt.studymatch.student.domain;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StudentTest {

    @Test
    void currentYearShouldBeOneWhenRegistrationYearIsCurrentYear() {
        Clock clock = Clock.fixed(
            Instant.parse("2024-06-15T12:00:00Z"),
            ZoneOffset.UTC
        );

        Student student = new Student(
            "2024001",
            "Aluno Teste",
            "aluno@example.com",
            2024,
            null
        );

        assertEquals(1, student.currentYear(clock));
    }

    @Test
    void currentYearShouldBeTwoOneYearAfterRegistration() {
        Clock clock = Clock.fixed(
            Instant.parse("2025-06-15T12:00:00Z"),
            ZoneOffset.UTC
        );

        Student student = new Student(
            "2024001",
            "Aluno Teste",
            "aluno@example.com",
            2024,
            null
        );

        assertEquals(2, student.currentYear(clock));
    }

    @Test
    void currentYearShouldBeThreeTwoYearsAfterRegistration() {
        Clock clock = Clock.fixed(
            Instant.parse("2026-06-15T12:00:00Z"),
            ZoneOffset.UTC
        );

        Student student = new Student(
            "2024001",
            "Aluno Teste",
            "aluno@example.com",
            2024,
            null
        );

        assertEquals(3, student.currentYear(clock));
    }
}
