package pt.upt.studymatch.academic.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pt.upt.studymatch.academic.application.AcademicHistoryApiService;
import pt.upt.studymatch.common.api.GlobalExceptionHandler;
import pt.upt.studymatch.common.config.ClockConfig;
import pt.upt.studymatch.student.application.StudentNotFoundException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AcademicHistoryController.class)
@Import({GlobalExceptionHandler.class, ClockConfig.class})
class AcademicHistoryControllerTest {
    @Autowired MockMvc mvc;
    @MockBean AcademicHistoryApiService service;

    @Test
    void returnsHistoryWithExpectedStructure() throws Exception {
        when(service.history(1L)).thenReturn(new HistoryResponse(List.of(
            new HistoryResponse.Year("2024/25", List.of(
                new HistoryResponse.Semester(1, List.of(
                    new HistoryResponse.Unit("DB1", "Bases de Dados I", 6,
                        List.of(new HistoryResponse.Attempt("NORMAL", 8), new HistoryResponse.Attempt("RECURSO", 14)), 14)
                ))
            ))
        )));
        mvc.perform(get("/api/students/1/history"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.years[0].academicYear").value("2024/25"))
            .andExpect(jsonPath("$.years[0].semesters[0].semester").value(1))
            .andExpect(jsonPath("$.years[0].semesters[0].units[0].attempts[1].season").value("RECURSO"))
            .andExpect(jsonPath("$.years[0].semesters[0].units[0].bestGrade").value(14));
    }


    @Test
    void returnsCourseUnits() throws Exception {
        when(service.courseUnits()).thenReturn(List.of(
            new CourseUnitResponse("DB1", "Bases de Dados I", 2, 1, 6)
        ));

        mvc.perform(get("/api/course-units"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].code").value("DB1"))
            .andExpect(jsonPath("$[0].name").value("Bases de Dados I"))
            .andExpect(jsonPath("$[0].ects").value(6));
    }

    @Test
    void createsAttempt() throws Exception {
        mvc.perform(post("/api/students/1/attempts").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"courseUnitCode":"DB1","academicYear":"2024/25","season":"NORMAL","grade":14}
                    """))
            .andExpect(status().isCreated());

        verify(service).addAttempt(eq(1L), any(AttemptRequest.class));
    }

    @Test
    void returns409ApiErrorWhenAttemptConflicts() throws Exception {
        doThrow(new DataIntegrityViolationException("duplicate attempt"))
            .when(service).addAttempt(eq(1L), any(AttemptRequest.class));

        mvc.perform(post("/api/students/1/attempts").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"courseUnitCode":"DB1","academicYear":"2024/25","season":"NORMAL","grade":14}
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void returns400ForInvalidAttempt() throws Exception {
        mvc.perform(post("/api/students/1/attempts").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"courseUnitCode":"DB1","academicYear":"2024/25","season":"NORMAL","grade":25}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void returns404WhenStudentDoesNotExist() throws Exception {
        when(service.history(404L)).thenThrow(new StudentNotFoundException(404L));
        mvc.perform(get("/api/students/404/history"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }
}
