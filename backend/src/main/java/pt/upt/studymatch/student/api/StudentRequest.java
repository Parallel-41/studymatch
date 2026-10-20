package pt.upt.studymatch.student.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pt.upt.studymatch.student.application.StudentCommand;

/** Request DTO for creating or updating a student. */
public record StudentRequest(
    @NotBlank(message = "number is required")
    @Size(max = 50, message = "number must not exceed 50 characters")
    String number,

    @NotBlank(message = "name is required")
    @Size(max = 255, message = "name must not exceed 255 characters")
    String name,

    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid email address")
    @Size(max = 255, message = "email must not exceed 255 characters")
    String email,

    @NotNull(message = "registrationYear is required")
    @Min(value = 1900, message = "registrationYear must be 1900 or later")
    Integer registrationYear,

    @NotBlank(message = "degreeProgramCode is required")
    @Size(max = 20, message = "degreeProgramCode must not exceed 20 characters")
    String degreeProgramCode
) {
    public StudentCommand toCommand() {
        return new StudentCommand(number, name, email, registrationYear, degreeProgramCode);
    }
}
