
package pt.upt.studymatch.student.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.time.Clock;
import pt.upt.studymatch.academic.domain.DegreeProgram;

@Entity
@Table(
    name = "student",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_student_number",
            columnNames = "number"
        ),
        @UniqueConstraint(
            name = "uk_student_email",
            columnNames = "email"
        )
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

    @NotNull
    @Min(1900)
    @Column(name = "registration_year", nullable = false)
    private Integer registrationYear;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "degree_program_id", nullable = false)
    private DegreeProgram degreeProgram;

    protected Student() {
        // Required by JPA
    }
    public int currentYear(Clock clock) {
        return clock.instant()
            .atZone(clock.getZone())
            .getYear() - registrationYear + 1;
    }
    public Student(
        String number,
        String name,
        String email,
        Integer registrationYear,
        DegreeProgram degreeProgram
    ) {
        this.number = number;
        this.name = name;
        this.email = email;
        this.registrationYear = registrationYear;
        this.degreeProgram = degreeProgram;
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

    public Integer getRegistrationYear() {
        return registrationYear;
    }

    public DegreeProgram getDegreeProgram() {
        return degreeProgram;
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

    public void setRegistrationYear(Integer registrationYear) {
        this.registrationYear = registrationYear;
    }

    public void setDegreeProgram(DegreeProgram degreeProgram) {
        this.degreeProgram = degreeProgram;
    }
}
