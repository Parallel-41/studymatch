package pt.upt.studymatch.academic.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import pt.upt.studymatch.academic.infrastructure.GradeAttributeConverter;
import pt.upt.studymatch.common.domain.Grade;

import java.time.LocalDate;

@Entity
@Table(name = "attempt")
// Representa uma tentativa de avaliação de uma inscrição numa unidade curricular.
public class Attempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrolment_id", nullable = false)
    private Enrolment enrolment;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssessmentSeason season;

    @Convert(converter = GradeAttributeConverter.class)
    @Column(name = "grade")
    private Grade grade;

    @NotNull
    @Column(nullable = false)
    private LocalDate date;

    protected Attempt() {
        // Required by JPA.
    }

    public Attempt(
        Enrolment enrolment,
        AssessmentSeason season,
        Grade grade,
        LocalDate date
    ) {
        this.enrolment = enrolment;
        this.season = season;
        this.grade = grade;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public Enrolment getEnrolment() {
        return enrolment;
    }

    public AssessmentSeason getSeason() {
        return season;
    }

    public Grade getGrade() {
        return grade;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setEnrolment(Enrolment enrolment) {
        this.enrolment = enrolment;
    }
}
