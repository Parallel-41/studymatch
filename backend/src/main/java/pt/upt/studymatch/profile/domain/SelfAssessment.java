package pt.upt.studymatch.profile.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "self_assessment")
public class SelfAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    @NotNull(message = "El ID del estudiante es obligatorio")
    private Long studentId;

    @Column(name = "area_code", nullable = false, length = 10)
    @NotBlank(message = "El código de área no puede estar vacío")
    private String areaCode;

    @Column(name = "level", nullable = false)
    @Min(value = 0, message = "El nivel no puede ser menor que 0")
    @Max(value = 100, message = "El nivel no puede ser mayor que 100")
    private Double level;

    @Column(name = "assessed_at", nullable = false)
    @NotNull(message = "La fecha de evaluación es obligatoria")
    private LocalDate assessedAt;

    // Constructor vacío exigido por JPA
    protected SelfAssessment() {
    }

    public SelfAssessment(Long studentId, String areaCode, Double level, LocalDate assessedAt) {
        this.studentId = Objects.requireNonNull(studentId, "studentId cannot be null");
        this.areaCode = Objects.requireNonNull(areaCode, "areaCode cannot be null");
        if (level == null || level < 0 || level > 100) {
            throw new IllegalArgumentException("Level must be between 0 and 100, got " + level);
        }
        this.level = level;
        this.assessedAt = Objects.requireNonNull(assessedAt, "assessedAt cannot be null");
    }

    public Long getId() { return id; }
    public Long getStudentId() { return studentId; }
    public String getAreaCode() { return areaCode; }
    public Double getLevel() { return level; }
    public LocalDate getAssessedAt() { return assessedAt; }

    public void updateLevel(Double newLevel) {
        if (newLevel == null || newLevel < 0 || newLevel > 100) {
            throw new IllegalArgumentException("Level must be between 0 and 100, got " + newLevel);
        }
        this.level = newLevel;
        this.assessedAt = LocalDate.now();
    }
}
