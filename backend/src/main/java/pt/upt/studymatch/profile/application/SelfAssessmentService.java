package pt.upt.studymatch.profile.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.common.domain.Level;
import pt.upt.studymatch.competency.infrastructure.CompetencyAreaRepository;
import pt.upt.studymatch.profile.domain.SelfAssessment;
import pt.upt.studymatch.profile.infrastructure.SelfAssessmentRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class SelfAssessmentService implements SelfAssessmentProvider {

    private final SelfAssessmentRepository repository;
    private final CompetencyAreaRepository areaRepository;
    private final Clock clock;

    public SelfAssessmentService(SelfAssessmentRepository repository,
                                 CompetencyAreaRepository areaRepository,
                                 Clock clock) {
        this.repository = repository;
        this.areaRepository = areaRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    @Override
    public Map<String, Level> levelsByArea(long studentId) {
        return repository.findAllByStudentId(studentId).stream()
            .collect(Collectors.toMap(
                SelfAssessment::getAreaCode,
                sa -> new Level(sa.getLevel())
            ));
    }

    public void saveSelfAssessments(long studentId, Map<String, Double> levels) {
        levels.forEach((area, value) -> {
            // Validar contra la base de datos real usando el método que acabas de crear
            if (!areaRepository.existsByCode(area)) {
                throw new IllegalArgumentException("Área desconocida: " + area);
            }
            if (value == null || value < 0 || value > 100) {
                throw new IllegalArgumentException("Nivel fuera de rango (0-100) para el área: " + area);
            }

            // Usar el Clock inyectado para generar la fecha de forma estable
            repository.findByStudentIdAndAreaCode(studentId, area).ifPresentOrElse(
                existing -> existing.updateLevel(value, LocalDate.now(clock)),
                () -> repository.save(new SelfAssessment(studentId, area, value, LocalDate.now(clock)))
            );
        });
    }
}
