package pt.upt.studymatch.academic.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import pt.upt.studymatch.academic.domain.AssessmentSeason;

public record AttemptRequest(
    @NotBlank String courseUnitCode,
    @NotBlank @Pattern(regexp = "\\d{4}/\\d{2}") String academicYear,
    @NotNull AssessmentSeason season,
    @NotNull @Min(0) @Max(20) Integer grade
) {}
