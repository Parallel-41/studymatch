package pt.upt.studymatch.student.application;

import org.springframework.stereotype.Service;
import pt.upt.studymatch.student.domain.Student;
import pt.upt.studymatch.student.infrastructure.StudentRepository;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<Student> findAll() {

        return studentRepository.findAll();
    }

    public Student findById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));
    }
}