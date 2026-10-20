package pt.upt.studymatch.competency.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "course_unit_competency")
public class CourseUnitCompetency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_unit_code", nullable = false)
    private String courseUnitCode;

    @Column(name = "competency_code", nullable = false)
    private String competencyCode;

    @Column(nullable = false)
    private Double weight;

    protected CourseUnitCompetency() {
    }

    public CourseUnitCompetency(String courseUnitCode, String competencyCode, Double weight) {
        if (courseUnitCode == null || courseUnitCode.isBlank()) {
            throw new IllegalArgumentException("Course unit code cannot be blank");
        }
        if (competencyCode == null || competencyCode.isBlank()) {
            throw new IllegalArgumentException("Competency code cannot be blank");
        }
        if (weight == null || weight <= 0.0 || weight > 1.0) {
            throw new IllegalArgumentException("Weight must be in range ]0, 1]");
        }
        this.courseUnitCode = courseUnitCode.trim();
        this.competencyCode = competencyCode.trim();
        this.weight = weight;
    }

    public Long getId() {
        return id;
    }

    public String getCourseUnitCode() {
        return courseUnitCode;
    }

    public String getCompetencyCode() {
        return competencyCode;
    }

    public Double getWeight() {
        return weight;
    }
}