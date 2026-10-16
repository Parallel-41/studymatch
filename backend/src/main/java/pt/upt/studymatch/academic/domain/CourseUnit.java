package pt.upt.studymatch.academic.domain;
import jakarta.persistence.*;

@Entity
@Table(name = "course_unit")
//Esta classe represneta uma disciplina ou unidade curricular de um curso
public class CourseUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "curricular_year", nullable = false)
    private int curricularYear;

    @Column(nullable = false)
    private int semester;

    @Column(nullable = false)
    private int ects;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "degree_program_id", nullable = false)
    private DegreeProgram degreeProgram;

    protected CourseUnit() {
    }

    public CourseUnit(
        String code,
        String name,
        int curricularYear,
        int semester,
        int ects,
        DegreeProgram degreeProgram) {
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
