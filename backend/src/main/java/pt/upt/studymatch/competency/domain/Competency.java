package pt.upt.studymatch.competency.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "competency")
public class Competency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "area_id", nullable = false)
    private CompetencyArea area;

    protected Competency() {
    }

    public Competency(String code, String name, CompetencyArea area) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Competency code cannot be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Competency name cannot be blank");
        }
        if (area == null) {
            throw new IllegalArgumentException("Competency area cannot be null");
        }
        this.code = code.trim();
        this.name = name.trim();
        this.area = area;
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

    public CompetencyArea getArea() {
        return area;
    }
}