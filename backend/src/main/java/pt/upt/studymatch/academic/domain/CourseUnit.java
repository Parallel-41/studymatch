package pt.upt.studymatch.academic.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
    name = "course_unit",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_course_unit_code",
        columnNames = "code"
    )
)
// Representa uma unidade curricular de um curso.
public class CourseUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 20)
    private String code;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String name;

    @Min(1)
    @Max(5)
    @Column(name = "curricular_year", nullable = false)
    private int curricularYear;

    @Min(1)
    @Max(2)
    @Column(nullable = false)
    private int semester;

    @Min(1)
    @Column(nullable = false)
    private int ects;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "degree_program_id", nullable = false)
    private DegreeProgram degreeProgram;

    protected CourseUnit() {
        // Required by JPA.
    }

    public CourseUnit(
        String code,
        String name,
        int curricularYear,
        int semester,
        int ects,
        DegreeProgram degreeProgram
    ) {
        this.code = code;
        this.name = name;
        this.curricularYear = curricularYear;
        this.semester = semester;
        this.ects = ects;
        this.degreeProgram = degreeProgram;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getCurricularYear() {
        return curricularYear;
    }

    public int getSemester() {
        return semester;
    }

    public int getEcts() {
        return ects;
    }

    public DegreeProgram getDegreeProgram() {
        return degreeProgram;
    }
}
