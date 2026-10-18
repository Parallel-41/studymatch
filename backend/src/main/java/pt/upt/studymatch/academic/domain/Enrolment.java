package pt.upt.studymatch.academic.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pt.upt.studymatch.common.domain.Grade;
import pt.upt.studymatch.student.domain.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(
    name = "enrolment",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_enrolment_student_course_year",
        columnNames = {"student_id", "course_unit_id", "academic_year"}
    )
)
// Representa uma inscrição numa unidade curricular.
public class Enrolment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_unit_id", nullable = false)
    private CourseUnit courseUnit;

    @NotBlank
    @Column(name = "academic_year", nullable = false, length = 7)
    private String academicYear;

    @NotNull
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
        // Required by JPA.
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
            throw new IllegalArgumentException("A tentativa não pode ser null.");
        }

        attempts.add(attempt);
        attempt.setEnrolment(this);
    }

    /** Returns the highest passing grade across all attempts, if one exists. */
    public Optional<Grade> bestPassingGrade() {
        Grade bestGrade = null;

        for (Attempt attempt : attempts) {
            if (attempt == null || attempt.getGrade() == null) {
                continue;
            }

            Grade grade = attempt.getGrade();
            if (grade.isPassing()
                && (bestGrade == null || grade.value() > bestGrade.value())) {
                bestGrade = grade;
            }
        }

        return Optional.ofNullable(bestGrade);
    }
}
