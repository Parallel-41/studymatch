package pt.upt.studymatch.student.api;

import org.springframework.web.bind.annotation.*;
import pt.upt.studymatch.student.application.StudentService;
import pt.upt.studymatch.student.domain.Student;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public List<Student> getAllStudents() {

        return studentService.findAll();
    }

    @GetMapping("/{id}")
    public Student getStudentById(@PathVariable Long id) {
        return studentService.findById(id);
    }
}