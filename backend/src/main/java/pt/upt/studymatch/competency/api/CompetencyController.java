package pt.upt.studymatch.competency.api;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.upt.studymatch.competency.application.CompetencyCatalogueService;

@RestController
@RequestMapping("/api/competencies")
public class CompetencyController {

    private final CompetencyCatalogueService catalogueService;

    public CompetencyController(CompetencyCatalogueService catalogueService) {
        this.catalogueService = catalogueService;
    }

    @GetMapping
    public List<CompetencyAreaResponse> findAll() {
        return catalogueService.findAllByArea();
    }
}