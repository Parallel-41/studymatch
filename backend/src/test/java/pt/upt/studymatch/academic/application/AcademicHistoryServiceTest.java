
package pt.upt.studymatch.academic.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import pt.upt.studymatch.academic.domain.CourseUnit;
import pt.upt.studymatch.academic.domain.Enrolment;
import pt.upt.studymatch.academic.infrastructure.EnrolmentRepository;
import pt.upt.studymatch.common.domain.Grade;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AcademicHistoryServiceTest {

    private EnrolmentRepository repository;
    private AcademicHistoryService service;

    @BeforeEach
    void setUp() {
        repository = mock(EnrolmentRepository.class);

        Clock clock = Clock.fixed(
            Instant.parse("2026-10-17T12:00:00Z"),
            ZoneOffset.UTC
        );

        service = new AcademicHistoryService(repository, clock);
    }

    @Test
    void shouldReturnEmptyListWhenStudentHasNoEnrolments() {
        when(repository.findByStudentId(1L))
            .thenReturn(List.of());

        assertTrue(service.evidenceGradesFor(1L).isEmpty());
    }

    @Test
    void shouldIgnoreEnrolmentsWithoutPassingGrades() {
        Enrolment enrolment = mock(Enrolment.class);

        when(enrolment.bestPassingGrade())
            .thenReturn(Optional.empty());

        when(repository.findByStudentId(1L))
            .thenReturn(List.of(enrolment));

        assertTrue(service.evidenceGradesFor(1L).isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
        "2026/2027, 1, 0",
        "2025/2026, 2, 1",
        "2025/2026, 1, 2",
        "2024/2025, 2, 3"
    })
    void shouldCalculateSemestersAgo(
        String academicYear,
        int semester,
        int expectedSemestersAgo) {

        Enrolment enrolment = mock(Enrolment.class);
        CourseUnit courseUnit = mock(CourseUnit.class);

        when(enrolment.bestPassingGrade())
            .thenReturn(Optional.of(new Grade(15)));

        when(enrolment.getAcademicYear())
            .thenReturn(academicYear);

        when(enrolment.getCourseUnit())
            .thenReturn(courseUnit);

        when(courseUnit.getId()).thenReturn(10L);
        when(courseUnit.getCode()).thenReturn("UC01");
        when(courseUnit.getName()).thenReturn("Programming");
        when(courseUnit.getSemester()).thenReturn(semester);

        when(repository.findByStudentId(1L))
            .thenReturn(List.of(enrolment));

        List<EvidenceGrade> result =
            service.evidenceGradesFor(1L);

        assertEquals(1, result.size());
        assertEquals(
            expectedSemestersAgo,
            result.get(0).semestersAgo()
        );
        assertEquals(new Grade(15), result.get(0).grade());
    }

    @Test
    void shouldRejectNullStudentId() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.evidenceGradesFor(null)
        );
    }
}
