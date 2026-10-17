package pt.upt.studymatch.profile.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.common.domain.Level;
import pt.upt.studymatch.profile.domain.SelfAssessment;
import pt.upt.studymatch.profile.infrastructure.SelfAssessmentRepository;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class SelfAssessmentService {

    private final SelfAssessmentRepository repository;

    // Los códigos de área oficiales publicados por Hugo en el Issue #46
    private static final Set<String> VALID_AREAS = Set.of("PROG", "DB", "WEB", "QUAL", "MATH", "SOFT");

    public SelfAssessmentService(SelfAssessmentRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Map<String, Level> levelsByArea(long studentId) {
        return repository.findAllByStudentId(studentId).stream()
            .collect(Collectors.toMap(
                SelfAssessment::getAreaCode,
                sa -> new Level(sa.getLevel())
            ));
    }

    public void saveSelfAssessments(long studentId, Map<String, Double> levels) {
        levels.forEach((area, value) -> {
            if (!VALID_AREAS.contains(area)) {
                throw new IllegalArgumentException("Área desconocida: " + area);
            }
            if (value == null || value < 0 || value > 100) {
                throw new IllegalArgumentException("Nivel fuera de rango (0-100) para el área: " + area);
            }

            // Si ya existe la actualiza, si no, crea una nueva
            repository.findByStudentIdAndAreaCode(studentId, area).ifPresentOrElse(
                existing -> existing.updateLevel(value),
                () -> repository.save(new SelfAssessment(studentId, area, value, LocalDate.now()))
            );
        });
    }
}
