package pt.upt.studymatch.student.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import pt.upt.studymatch.academic.domain.DegreeProgram;
import pt.upt.studymatch.common.api.GlobalExceptionHandler;
import pt.upt.studymatch.common.config.ClockConfig;
import pt.upt.studymatch.common.api.HealthController;
import pt.upt.studymatch.student.application.DuplicateStudentException;
import pt.upt.studymatch.student.application.StudentCommand;
import pt.upt.studymatch.student.application.StudentNotFoundException;
import pt.upt.studymatch.student.application.StudentService;
import pt.upt.studymatch.student.domain.Student;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {HealthController.class, StudentController.class})
@Import({GlobalExceptionHandler.class, ClockConfig.class})
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StudentService studentService;

    @Test
    void healthEndpointReturnsUp() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.service").value("studymatch"));
    }

    @Test
    void getStudentsReturnsOnlyStudentResponseFields() throws Exception {
        when(studentService.findAll()).thenReturn(List.of(student(1L)));

        mockMvc.perform(get("/api/students"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].number").value("2024001"))
            .andExpect(jsonPath("$[0].degreeProgram.code").value("LEI"))
            .andExpect(jsonPath("$[0].registrationYear").value(2024))
            .andExpect(jsonPath("$[0].currentYear").exists())
            .andExpect(jsonPath("$[0].degreeProgram.id").doesNotExist());
    }

    @Test
    void postCreatesStudentAndReturns201WithLocation() throws Exception {
        when(studentService.create(any(StudentCommand.class))).thenReturn(student(27L));

        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest()))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/students/27")))
            .andExpect(jsonPath("$.id").value(27))
            .andExpect(jsonPath("$.number").value("2024001"))
            .andExpect(jsonPath("$.degreeProgram.code").value("LEI"));
    }

    @Test
    void putUpdatesStudentAndReturnsResponseDto() throws Exception {
        when(studentService.update(org.mockito.ArgumentMatchers.eq(27L), any(StudentCommand.class)))
            .thenReturn(student(27L));

        mockMvc.perform(put("/api/students/27")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(27))
            .andExpect(jsonPath("$.number").value("2024001"));
    }

    @Test
    void invalidEmailReturns400BeforeCallingService() throws Exception {
        String invalidEmail = validRequest().replace("joao.silva@example.com", "not-an-email");

        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidEmail))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("email")));

        verifyNoInteractions(studentService);
    }

    @Test
    void missingRequiredFieldsReturn400() throws Exception {
        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(studentService);
    }

    @Test
    void unknownStudentReturns404() throws Exception {
        when(studentService.findById(999L)).thenThrow(new StudentNotFoundException(999L));

        mockMvc.perform(get("/api/students/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void duplicateStudentReturns409() throws Exception {
        when(studentService.create(any(StudentCommand.class)))
            .thenThrow(new DuplicateStudentException());

        mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest()))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void unexpectedFailureReturnsGeneric500WithoutInternalDetails() throws Exception {
        when(studentService.findAll()).thenThrow(new IllegalStateException("database secret"));

        mockMvc.perform(get("/api/students"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.message").value("An unexpected error occurred."))
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("database secret"))));
    }

    private Student student(Long id) {
        Student student = new Student(
            "2024001",
            "João Silva",
            "joao.silva@example.com",
            2024,
            new DegreeProgram("LEI", "Licenciatura em Engenharia Informática")
        );
        ReflectionTestUtils.setField(student, "id", id);
        return student;
    }

    private String validRequest() throws Exception {
        return objectMapper.writeValueAsString(new StudentRequest(
            "2024001",
            "João Silva",
            "joao.silva@example.com",
            2024,
            "LEI"
        ));
    }

   @Test
    void getStudentsWithQueryReturnsFilteredList() throws Exception {
        when(studentService.search("2024", null)).thenReturn(List.of(student(1L)));

        mockMvc.perform(get("/api/students").param("q", "2024"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].number").value("2024001"));
    }

    
}
