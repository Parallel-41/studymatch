package pt.upt.studymatch.student.domain;

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
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pt.upt.studymatch.academic.domain.DegreeProgram;

import java.time.Clock;
import java.time.LocalDate;

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
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String number;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String name;

    @NotBlank
    @Email
    @Size(max = 255)
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
        // Required by JPA.
    }

    public Student(
        String number,
        String name,
        String email,
        Integer registrationYear,
        DegreeProgram degreeProgram
    ) {
        validateFields(number, name, email, registrationYear, degreeProgram);
        this.number = number;
        this.name = name;
        this.email = email;
        this.registrationYear = registrationYear;
        this.degreeProgram = degreeProgram;
    }

    /** Validates time-dependent domain rules before the entity is persisted. */
    public void validateFor(Clock clock) {
        validateFields(number, name, email, registrationYear, degreeProgram);
        if (registrationYear > academicYearStart(clock)) {
            throw new IllegalArgumentException("registrationYear cannot be in the future academic year.");
        }
    }

    public void updateDetails(
        String number,
        String name,
        String email,
        Integer registrationYear,
        DegreeProgram degreeProgram,
        Clock clock
    ) {
        validateFields(number, name, email, registrationYear, degreeProgram);
        if (registrationYear > academicYearStart(clock)) {
            throw new IllegalArgumentException("registrationYear cannot be in the future academic year.");
        }
        this.number = number;
        this.name = name;
        this.email = email;
        this.registrationYear = registrationYear;
        this.degreeProgram = degreeProgram;
    }

    private static void validateFields(
        String number,
        String name,
        String email,
        Integer registrationYear,
        DegreeProgram degreeProgram
    ) {
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("number is required.");
        }
        if (number.length() > 50) {
            throw new IllegalArgumentException("number must not exceed 50 characters.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required.");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("name must not exceed 255 characters.");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("email must be a valid email address.");
        }
        if (email.length() > 255) {
            throw new IllegalArgumentException("email must not exceed 255 characters.");
        }
        if (registrationYear == null || registrationYear < 1900) {
            throw new IllegalArgumentException("registrationYear must be 1900 or later.");
        }
        if (degreeProgram == null) {
            throw new IllegalArgumentException("degreeProgram is required.");
        }
    }

    private static boolean isValidEmail(String email) {
        if (email == null || email.isBlank() || email.chars().anyMatch(Character::isWhitespace)) {
            return false;
        }
        int at = email.indexOf('@');
        int lastDot = email.lastIndexOf('.');
        return at > 0 && at == email.lastIndexOf('@')
            && lastDot > at + 1 && lastDot < email.length() - 1;
    }

    private static int academicYearStart(Clock clock) {
        LocalDate today = LocalDate.now(clock);
        return today.getMonthValue() >= 9 ? today.getYear() : today.getYear() - 1;
    }

    public int currentYear(Clock clock) {
        return academicYearStart(clock) - registrationYear + 1;
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
        if (number == null || number.isBlank() || number.length() > 50) {
            throw new IllegalArgumentException("number must be non-blank and at most 50 characters.");
        }
        this.number = number;
    }

    public void setName(String name) {
        if (name == null || name.isBlank() || name.length() > 255) {
            throw new IllegalArgumentException("name must be non-blank and at most 255 characters.");
        }
        this.name = name;
    }

    public void setEmail(String email) {
        if (!isValidEmail(email) || email.length() > 255) {
            throw new IllegalArgumentException("email must be valid and at most 255 characters.");
        }
        this.email = email;
    }

    public void setRegistrationYear(Integer registrationYear) {
        if (registrationYear == null || registrationYear < 1900) {
            throw new IllegalArgumentException("registrationYear must be 1900 or later.");
        }
        this.registrationYear = registrationYear;
    }

    public void setDegreeProgram(DegreeProgram degreeProgram) {
        if (degreeProgram == null) {
            throw new IllegalArgumentException("degreeProgram is required.");
        }
        this.degreeProgram = degreeProgram;
    }
}
