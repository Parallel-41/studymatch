package pt.upt.studymatch.student.application;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.academic.domain.DegreeProgram;
import pt.upt.studymatch.academic.infrastructure.DegreeProgramRepository;
import pt.upt.studymatch.student.domain.Student;
import pt.upt.studymatch.student.infrastructure.StudentRepository;

import java.time.Clock;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final DegreeProgramRepository degreeProgramRepository;
    private final Clock clock;

    public StudentService(
        StudentRepository studentRepository,
        DegreeProgramRepository degreeProgramRepository,
        Clock clock
    ) {
        this.studentRepository = studentRepository;
        this.degreeProgramRepository = degreeProgramRepository;
        this.clock = clock;
    }

    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    public Student findById(Long id) {
        return studentRepository.findById(id)
            .orElseThrow(() -> new StudentNotFoundException(id));
    }

    @Transactional
    public Student create(StudentCommand command) {
        if (studentRepository.existsByNumber(command.number())
            || studentRepository.existsByEmail(command.email())) {
            throw new DuplicateStudentException();
        }

        DegreeProgram degreeProgram = findDegreeProgram(command.degreeProgramCode());
        Student student = new Student(
            command.number(),
            command.name(),
            command.email(),
            command.registrationYear(),
            degreeProgram
        );
        student.validateFor(clock);
        return saveHandlingDuplicates(student);
    }

    @Transactional
    public Student update(Long id, StudentCommand command) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new StudentNotFoundException(id));

        if (studentRepository.existsByNumberAndIdNot(command.number(), id)
            || studentRepository.existsByEmailAndIdNot(command.email(), id)) {
            throw new DuplicateStudentException();
        }

        DegreeProgram degreeProgram = findDegreeProgram(command.degreeProgramCode());
        student.updateDetails(
            command.number(),
            command.name(),
            command.email(),
            command.registrationYear(),
            degreeProgram,
            clock
        );
        return saveHandlingDuplicates(student);
    }

    private DegreeProgram findDegreeProgram(String code) {
        return degreeProgramRepository.findByCode(code)
            .orElseThrow(() -> new IllegalArgumentException(
                "Unknown degreeProgramCode: " + code
            ));
    }

    private Student saveHandlingDuplicates(Student student) {
        try {
            return studentRepository.saveAndFlush(student);
        } catch (DataIntegrityViolationException exception) {
            // Protect against two concurrent requests passing the pre-check simultaneously.
            throw new DuplicateStudentException();
        }
    }

    public List<Student> search(String q, Integer year) {
        List<Student> students;
        if (q != null && !q.isBlank()) {
            students = studentRepository.findByNameContainingIgnoreCaseOrNumberContainingIgnoreCase(q, q);
        } else {
            students = studentRepository.findAll();
        }

        if (year != null) {
            return students.stream()
                .filter(student -> student.currentYear(clock) == year)
                .toList();
        }

        return students;
    }
}
