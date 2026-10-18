package pt.upt.studymatch.competency.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "competency_area")
public class CompetencyArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    protected CompetencyArea() {
    }

    public CompetencyArea(String code, String name) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Competency area code cannot be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Competency area name cannot be blank");
        }
        this.code = code.trim();
        this.name = name.trim();
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