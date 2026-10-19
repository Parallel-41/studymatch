package pt.upt.studymatch.student.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pt.upt.studymatch.academic.domain.DegreeProgram;
import pt.upt.studymatch.academic.infrastructure.DegreeProgramRepository;
import pt.upt.studymatch.student.domain.Student;
import pt.upt.studymatch.student.infrastructure.StudentRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private DegreeProgramRepository degreeProgramRepository;

    private StudentService studentService;
    private DegreeProgram degreeProgram;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
            Instant.parse("2026-10-19T09:00:00Z"),
            ZoneOffset.UTC
        );
        studentService = new StudentService(studentRepository, degreeProgramRepository, clock);
        degreeProgram = new DegreeProgram("LEI", "Licenciatura em Engenharia Informática");
    }

    @Test
    void createsStudentWithResolvedDegreeProgram() {
        when(studentRepository.existsByNumber("2024001")).thenReturn(false);
        when(studentRepository.existsByEmail("joao@example.com")).thenReturn(false);
        when(degreeProgramRepository.findByCode("LEI")).thenReturn(Optional.of(degreeProgram));
        when(studentRepository.saveAndFlush(any(Student.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Student created = studentService.create(command("2024001", "joao@example.com", 2024));

        assertEquals("2024001", created.getNumber());
        assertEquals("João Silva", created.getName());
        assertEquals("joao@example.com", created.getEmail());
        assertEquals(2024, created.getRegistrationYear());
        assertEquals("LEI", created.getDegreeProgram().getCode());
        verify(studentRepository).saveAndFlush(created);
    }

    @Test
    void rejectsDuplicateStudentNumber() {
        when(studentRepository.existsByNumber("2024001")).thenReturn(true);

        assertThrows(
            DuplicateStudentException.class,
            () -> studentService.create(command("2024001", "joao@example.com", 2024))
        );

        verify(studentRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsDuplicateStudentEmail() {
        when(studentRepository.existsByNumber("2024001")).thenReturn(false);
        when(studentRepository.existsByEmail("joao@example.com")).thenReturn(true);

        assertThrows(
            DuplicateStudentException.class,
            () -> studentService.create(command("2024001", "joao@example.com", 2024))
        );

        verify(studentRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsFutureRegistrationYear() {
        when(studentRepository.existsByNumber("2024001")).thenReturn(false);
        when(studentRepository.existsByEmail("joao@example.com")).thenReturn(false);
        when(degreeProgramRepository.findByCode("LEI")).thenReturn(Optional.of(degreeProgram));

        assertThrows(
            IllegalArgumentException.class,
            () -> studentService.create(command("2024001", "joao@example.com", 2027))
        );

        verify(studentRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsUnknownDegreeProgramCode() {
        when(studentRepository.existsByNumber("2024001")).thenReturn(false);
        when(studentRepository.existsByEmail("joao@example.com")).thenReturn(false);
        when(degreeProgramRepository.findByCode("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(
            IllegalArgumentException.class,
            () -> studentService.create(new StudentCommand(
                "2024001", "João Silva", "joao@example.com", 2024, "UNKNOWN"
            ))
        );
    }

    @Test
    void updatesExistingStudentDetails() {
        Student existing = new Student(
            "OLD-001", "Old Name", "old@example.com", 2023, degreeProgram
        );
        ReflectionTestUtils.setField(existing, "id", 27L);
        when(studentRepository.findById(27L)).thenReturn(Optional.of(existing));
        when(studentRepository.existsByNumberAndIdNot("2024001", 27L)).thenReturn(false);
        when(studentRepository.existsByEmailAndIdNot("joao@example.com", 27L)).thenReturn(false);
        when(degreeProgramRepository.findByCode("LEI")).thenReturn(Optional.of(degreeProgram));
        when(studentRepository.saveAndFlush(any(Student.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Student updated = studentService.update(
            27L, command("2024001", "joao@example.com", 2024)
        );

        assertEquals(27L, updated.getId());
        assertEquals("2024001", updated.getNumber());
        assertEquals("joao@example.com", updated.getEmail());
        assertEquals(2024, updated.getRegistrationYear());
        verify(studentRepository).saveAndFlush(existing);
    }

    @Test
    void updateReturns404WhenStudentDoesNotExist() {
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
            StudentNotFoundException.class,
            () -> studentService.update(999L, command("2024001", "joao@example.com", 2024))
        );
    }

    private StudentCommand command(String number, String email, Integer year) {
        return new StudentCommand(number, "João Silva", email, year, "LEI");
    }
}
