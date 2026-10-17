package pt.upt.studymatch.academic.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(
    name = "degree_program",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_degree_program_code",
        columnNames = "code"
    )
)
// Representa um curso conferente de grau académico.
public class DegreeProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 20)
    private String code;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String name;

    protected DegreeProgram() {
        // Required by JPA.
    }

    public DegreeProgram(String code, String name) {
        this.code = code;
        this.name = name;
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
}
