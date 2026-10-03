package pt.upt.studymatch.common.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(StudentNotFoundException.class)
    public org.springframework.http.ResponseEntity<ApiError> handleStudentNotFound(
            StudentNotFoundException exception,
            org.springframework.web.context.request.WebRequest request
    ) {
        ApiError error = new ApiError(
                Instant.now(clock),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                exception.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return org.springframework.http.ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }
}