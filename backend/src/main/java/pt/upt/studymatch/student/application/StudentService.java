package pt.upt.studymatch.student.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.student.api.StudentResponse;
import pt.upt.studymatch.student.domain.Student;
import pt.upt.studymatch.student.infrastructure.StudentRepository;

import java.time.Clock;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final Clock clock;

    public StudentService(StudentRepository studentRepository, Clock clock) {
        this.studentRepository = studentRepository;
        this.clock = clock;
    }

    public List<StudentResponse> findAll() {
        return studentRepository.findAll()
            .stream()
            .map(student -> StudentResponse.from(student, clock))
            .toList();
    }

    public StudentResponse findById(Long id) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new StudentNotFoundException(id));

        return StudentResponse.from(student, clock);
    }
}
