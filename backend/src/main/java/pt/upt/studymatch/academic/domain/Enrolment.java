
package pt.upt.studymatch.academic.domain;

import jakarta.persistence.*;
import pt.upt.studymatch.student.domain.Student;

import java.util.ArrayList;
import java.util.List;

import java.math.BigDecimal;
import java.util.Optional;

@Entity
@Table(name = "enrolment")
// Esta classe representa uma incriçao em uma unidade curricular
public class Enrolment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_unit_id", nullable = false)
    private CourseUnit courseUnit;

    @Column(name = "academic_year", nullable = false, length = 7)
    private String academicYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrolmentStatus status;

    @OneToMany(
        mappedBy = "enrolment",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Attempt> attempts = new ArrayList<>();

    protected Enrolment() {
    }

    public Enrolment(
        Student student,
        CourseUnit courseUnit,
        String academicYear,
        EnrolmentStatus status
    ) {
        this.student = student;
        this.courseUnit = courseUnit;
        this.academicYear = academicYear;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public CourseUnit getCourseUnit() {
        return courseUnit;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public EnrolmentStatus getStatus() {
        return status;
    }

    public List<Attempt> getAttempts() {
        return attempts;
    }

    public void addAttempt(Attempt attempt) {
        if (attempt == null) {
            throw new IllegalArgumentException(
                "A tentativa não pode ser null."
            );
        }

        attempts.add(attempt);
        attempt.setEnrolment(this);
    }

    public Optional<BigDecimal> bestPassingGrade() {
        BigDecimal bestGrade = null;

        for (Attempt attempt : attempts) {
            if (attempt != null
                && attempt.getGrade() != null
                && attempt.getGrade().compareTo(BigDecimal.TEN) >= 0) {

                if (bestGrade == null
                    || attempt.getGrade().compareTo(bestGrade) > 0) {
                    bestGrade = attempt.getGrade();
                }
            }
        }

        return Optional.ofNullable(bestGrade);
    }
}
