package pt.upt.studymatch.profile.api;

import java.util.Map;

// Record que mapea exactamente el JSON esperado por el frontend
public record SelfAssessmentDto(Map<String, Double> levels) {
}
