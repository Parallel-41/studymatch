package pt.upt.studymatch.student.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pt.upt.studymatch.student.application.StudentService;
import pt.upt.studymatch.student.domain.Student;

import java.net.URI;
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
    public List<StudentResponse> getAllStudents(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) Integer year
    ) {
        List<Student> students = (q != null && !q.isBlank() || year != null)
            ? studentService.search(q, year)
            : studentService.findAll();

        return students.stream()
            .map(student -> StudentResponse.from(student, clock))
            .toList();
    }

    @GetMapping("/{id}")
    public StudentResponse getStudentById(@PathVariable Long id) {
        Student student = studentService.findById(id);
        return StudentResponse.from(student, clock);
    }

    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(
        @Valid @RequestBody StudentRequest request
    ) {
        Student saved = studentService.create(request.toCommand());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(saved.getId())
            .toUri();
        return ResponseEntity.created(location)
            .body(StudentResponse.from(saved, clock));
    }

    @PutMapping("/{id}")
    public StudentResponse updateStudent(
        @PathVariable Long id,
        @Valid @RequestBody StudentRequest request
    ) {
        Student updated = studentService.update(id, request.toCommand());
        return StudentResponse.from(updated, clock);
    }
}
