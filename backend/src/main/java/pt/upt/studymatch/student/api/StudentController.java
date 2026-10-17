package pt.upt.studymatch.student.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.upt.studymatch.student.application.StudentService;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public List<StudentResponse> getAllStudents() {
        return studentService.findAll();
    }

    @GetMapping("/{id}")
    public StudentResponse getStudentById(@PathVariable Long id) {
        return studentService.findById(id);
    }
}
