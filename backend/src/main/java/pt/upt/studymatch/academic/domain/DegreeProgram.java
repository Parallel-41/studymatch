
package pt.upt.studymatch.academic.domain;

import jakarta.persistence.*;
// Esta classe representa o curso em que um estudante está inscrito
@Entity
@Table(name = "degree_program")
public class DegreeProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false)
    private String name;

    protected DegreeProgram() {
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
