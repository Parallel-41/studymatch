package pt.upt.studymatch.student.api;

import org.junit.jupiter.api.Test;
import pt.upt.studymatch.academic.domain.DegreeProgram;
import pt.upt.studymatch.student.domain.Student;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class StudentResponseTest {

    @Test
    void mapsStudentAndDegreeProgramToResponse() {
        DegreeProgram program = new DegreeProgram(
            "LEI",
            "Licenciatura em Engenharia Informática"
        );

        Student student = new Student(
            "2024001",
            "João Silva",
            "joao.silva@example.com",
            2025,
            program
        );

        Clock clock = Clock.fixed(
            Instant.parse("2026-10-17T12:00:00Z"),
            ZoneOffset.UTC
        );

        StudentResponse response = StudentResponse.from(student, clock);

        assertEquals("2024001", response.number());
        assertEquals("João Silva", response.name());
        assertEquals("joao.silva@example.com", response.email());
        assertEquals(2025, response.registrationYear());
        assertEquals(2, response.currentYear());

        assertNotNull(response.degreeProgram());
        assertEquals("LEI", response.degreeProgram().code());
        assertEquals(
            "Licenciatura em Engenharia Informática",
            response.degreeProgram().name()
        );
    }
}
