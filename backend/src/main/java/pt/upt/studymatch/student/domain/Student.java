package pt.upt.studymatch.student.domain;

import jakarta.persistence.*;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(
        name = "student",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_student_number", columnNames = "number"),
                @UniqueConstraint(name = "uk_student_email", columnNames = "email")
        }
)
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String number;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String name;

    @NotBlank
    @Email
    @Column(nullable = false, length = 255)
    private String email;

    @Min(1)
    @Max(5)
    @Column(name = "current_year", nullable = false)
    private Integer currentYear;

    protected Student() {
        // Required by JPA
    }

    public Student(String number, String name, String email, Integer currentYear) {
        this.number = number;
        this.name = name;
        this.email = email;
        this.currentYear = currentYear;
    }

    public Long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Integer getCurrentYear() {
        return currentYear;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCurrentYear(Integer currentYear) {
        this.currentYear = currentYear;
    }
}