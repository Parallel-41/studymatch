package pt.upt.studymatch.student.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.upt.studymatch.student.application.StudentService;
import pt.upt.studymatch.student.domain.Student;

import java.time.Clock;
import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;
    private final Clock clock;

    public StudentController(StudentService studentService, Clock clock) {
        this.studentService = studentService;
        this.clock = clock;
    }

    @GetMapping
    public List<StudentResponse> getAllStudents() {
        return studentService.findAll().stream()
            .map(student -> StudentResponse.from(student, clock))
            .toList();
    }

    @GetMapping("/{id}")
    public StudentResponse getStudentById(@PathVariable Long id) {
        Student student = studentService.findById(id);
        return StudentResponse.from(student, clock);
    }
}
