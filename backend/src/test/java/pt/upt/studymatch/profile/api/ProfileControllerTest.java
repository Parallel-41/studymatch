package pt.upt.studymatch.profile.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pt.upt.studymatch.common.api.GlobalExceptionHandler;
import pt.upt.studymatch.common.config.ClockConfig;
import pt.upt.studymatch.profile.application.ProfileService;
import pt.upt.studymatch.profile.application.CompetencyNotFoundException;
import pt.upt.studymatch.profile.application.ExplanationView;
import pt.upt.studymatch.profile.application.ProfileView;
import pt.upt.studymatch.profile.domain.Contribution;
import pt.upt.studymatch.student.application.StudentNotFoundException;

/** The response must keep the structure of frontend/src/api/examples/profile.json. */
@WebMvcTest(controllers = ProfileController.class)
@Import({GlobalExceptionHandler.class, ClockConfig.class})
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileService profileService;

    @Test
    void returnsProfileWithTheSameStructureAsTheExample() throws Exception {
        when(profileService.profileFor(1L)).thenReturn(new ProfileView(
            List.of(new ProfileView.AreaView("PROG", "Programming", 54.95, 0.3055),
                new ProfileView.AreaView("DB", "Databases", null, 0)),
            List.of(new ProfileView.CompetencyView("PROG_OO", "Object-Oriented Programming", "PROG",
                    59.8625, 0.61125, 66.13496, 50.0),
                new ProfileView.CompetencyView("DB_SQL", "Relational Databases & SQL", "DB",
                    null, 0, null, null))));

        mockMvc.perform(get("/api/students/1/profile"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.areas[0].code").value("PROG"))
            .andExpect(jsonPath("$.areas[0].name").value("Programming"))
            .andExpect(jsonPath("$.areas[0].level").value(55.0))
            .andExpect(jsonPath("$.areas[0].confidence").value(0.31))
            .andExpect(jsonPath("$.areas[1].level").isEmpty())
            .andExpect(jsonPath("$.competencies[0].code").value("PROG_OO"))
            .andExpect(jsonPath("$.competencies[0].area").value("PROG"))
            .andExpect(jsonPath("$.competencies[0].level").value(59.9))
            .andExpect(jsonPath("$.competencies[0].confidence").value(0.61))
            .andExpect(jsonPath("$.competencies[0].evidenceLevel").value(66.1))
            .andExpect(jsonPath("$.competencies[0].selfAssessedLevel").value(50.0))
            .andExpect(jsonPath("$.competencies[1].level").isEmpty())
            .andExpect(jsonPath("$.competencies[1].confidence").value(0.0));
    }

    @Test
    void unknownStudentReturns404() throws Exception {
        when(profileService.profileFor(99L)).thenThrow(new StudentNotFoundException(99L));

        mockMvc.perform(get("/api/students/99/profile"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void returnsExplanationWithTheSameStructureAsTheExample() throws Exception {
        when(profileService.explanationFor(1L, "PROG_OO")).thenReturn(new ExplanationView(
            "PROG_OO", "Object-Oriented Programming", 59.8625, 0.61125, 66.13496, 50.0, 0.38875,
            List.of(new Contribution("PROG1", 6, 12, 1.0, 0.7225, 4.335, 35.4601, 0.53620),
                new Contribution("POO", 6, 15, 0.5, 1.0, 3.0, 30.6748, 0.46380))));

        mockMvc.perform(get("/api/students/1/profile/competencies/PROG_OO/explanation"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.competencyCode").value("PROG_OO"))
            .andExpect(jsonPath("$.level").value(59.9))
            .andExpect(jsonPath("$.confidence").value(0.61))
            .andExpect(jsonPath("$.evidenceLevel").value(66.1))
            .andExpect(jsonPath("$.selfAssessedLevel").value(50.0))
            .andExpect(jsonPath("$.selfAssessedShare").value(0.39))
            .andExpect(jsonPath("$.contributions[0].courseUnitCode").value("PROG1"))
            .andExpect(jsonPath("$.contributions[0].ects").value(6))
            .andExpect(jsonPath("$.contributions[0].grade").value(12))
            .andExpect(jsonPath("$.contributions[0].weight").value(1.0))
            .andExpect(jsonPath("$.contributions[0].recency").value(0.72))
            .andExpect(jsonPath("$.contributions[0].points").value(35.5))
            .andExpect(jsonPath("$.contributions[0].share").value(0.54))
            .andExpect(jsonPath("$.contributions[1].share").value(0.46));
    }

    @Test
    void unknownCompetencyReturns404() throws Exception {
        when(profileService.explanationFor(1L, "XYZ")).thenThrow(new CompetencyNotFoundException("XYZ"));

        mockMvc.perform(get("/api/students/1/profile/competencies/XYZ/explanation"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }
}
