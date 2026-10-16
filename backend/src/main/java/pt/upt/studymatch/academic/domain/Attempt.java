
package pt.upt.studymatch.academic.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "attempt")
// Esta classe representa uma tentativa de avaliação de uma inscrição numa unidade curricular.
public class Attempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrolment_id", nullable = false)
    private Enrolment enrolment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssessmentSeason season;

    @Column(precision = 4, scale = 2)
    private BigDecimal grade;

    @Column(nullable = false)
    private LocalDate date;

    protected Attempt() {
        // Construtor exigido pelo JPA
    }

    public Attempt(
        Enrolment enrolment,
        AssessmentSeason season,
        BigDecimal grade,
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

    public BigDecimal getGrade() {
        return grade;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setEnrolment(Enrolment enrolment) {
        this.enrolment = enrolment;
    }
}
