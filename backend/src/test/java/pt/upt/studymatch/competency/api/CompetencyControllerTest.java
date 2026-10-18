package pt.upt.studymatch.competency.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock; // <-- NUEVO
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pt.upt.studymatch.competency.application.CompetencyCatalogueService;

@WebMvcTest(CompetencyController.class)
class CompetencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompetencyCatalogueService catalogueService;

    @MockitoBean
    private Clock clock; // <-- NUEVO: Mockeamos el reloj para el GlobalExceptionHandler de Guilherme

    @Test
    void shouldReturnCompetenciesGroupedByArea() throws Exception {
        List<CompetencyAreaResponse> sampleResponse = List.of(
                new CompetencyAreaResponse(
                        "PROG",
                        "Programming",
                        List.of(new CompetencyResponse("PROG_OO", "Object-Oriented Programming"))));

        when(catalogueService.findAllByArea()).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/competencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("PROG"))
                .andExpect(jsonPath("$[0].name").value("Programming"))
                .andExpect(jsonPath("$[0].competencies[0].code").value("PROG_OO"))
                .andExpect(jsonPath("$[0].competencies[0].name").value("Object-Oriented Programming"));
    }
}