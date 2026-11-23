package pt.upt.studymatch.profile.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pt.upt.studymatch.common.domain.Level;
import pt.upt.studymatch.profile.application.SelfAssessmentService;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/students/{studentId}/self-assessment")
public class SelfAssessmentController {

    private final SelfAssessmentService service;

    public SelfAssessmentController(SelfAssessmentService service) {
        this.service = service;
    }

    @GetMapping
    public SelfAssessmentDto getSelfAssessment(@PathVariable Long studentId) {
        return fetchCurrentLevels(studentId);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public SelfAssessmentDto saveSelfAssessment(
        @PathVariable Long studentId,
        @RequestBody SelfAssessmentDto request) {

        // Enviamos las notas del JSON al servicio para validarlas y guardarlas
        service.saveSelfAssessments(studentId, request.levels());

        // Devolvemos el estado actualizado
        return fetchCurrentLevels(studentId);
    }

    // Método auxiliar para convertir los objetos Level del dominio de vuelta a números para el JSON
    private SelfAssessmentDto fetchCurrentLevels(Long studentId) {
        Map<String, Level> domainLevels = service.levelsByArea(studentId);
        Map<String, Double> rawLevels = domainLevels.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().value()
            ));
        return new SelfAssessmentDto(rawLevels);
    }
}
