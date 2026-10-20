package pt.upt.studymatch.academic.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pt.upt.studymatch.academic.application.AcademicHistoryApiService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AcademicHistoryController {
    private final AcademicHistoryApiService service;

    public AcademicHistoryController(AcademicHistoryApiService service) {
        this.service = service;
    }

    @GetMapping("/students/{id}/history")
    public HistoryResponse history(@PathVariable Long id) {
        return service.history(id);
    }

    @PostMapping("/students/{id}/attempts")
    @ResponseStatus(HttpStatus.CREATED)
    public void addAttempt(@PathVariable Long id, @Valid @RequestBody AttemptRequest request) {
        service.addAttempt(id, request);
    }

    @GetMapping("/course-units")
    public List<CourseUnitResponse> courseUnits() {
        return service.courseUnits();
    }
}
