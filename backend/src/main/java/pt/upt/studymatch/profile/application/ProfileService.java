package pt.upt.studymatch.profile.application;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.academic.application.AcademicHistoryService;
import pt.upt.studymatch.academic.domain.CourseUnit;
import pt.upt.studymatch.academic.domain.EvidenceGrade;
import pt.upt.studymatch.academic.infrastructure.CourseUnitRepository;
import pt.upt.studymatch.common.domain.Level;
import pt.upt.studymatch.competency.application.CompetencyCatalogueService;
import pt.upt.studymatch.competency.domain.Competency;
import pt.upt.studymatch.competency.domain.CourseUnitWeight;
import pt.upt.studymatch.competency.infrastructure.CompetencyRepository;
import pt.upt.studymatch.profile.domain.CompetencyExplanation;
import pt.upt.studymatch.profile.domain.CompetencyLevel;
import pt.upt.studymatch.profile.domain.ProfileCalculator;
import pt.upt.studymatch.profile.domain.StudentProfile;
import pt.upt.studymatch.student.application.StudentNotFoundException;
import pt.upt.studymatch.student.domain.Student;
import pt.upt.studymatch.student.infrastructure.StudentRepository;

/**
 * Builds a student's competency profile on every request (ADR-0003, A4: computed, never stored).
 * Loads the evidence, the weights of the student's degree programme, the self-assessment and
 * the catalogue, then delegates the formula to {@link ProfileCalculator}.
 */
@Service
@Transactional(readOnly = true)
public class ProfileService {

    private final StudentRepository studentRepository;
    private final CourseUnitRepository courseUnitRepository;
    private final CompetencyRepository competencyRepository;
    private final AcademicHistoryService academicHistoryService;
    private final CompetencyCatalogueService competencyCatalogueService;
    private final ObjectProvider<SelfAssessmentProvider> selfAssessmentProvider;
    private final ProfileCalculator calculator = new ProfileCalculator();

    public ProfileService(StudentRepository studentRepository,
                          CourseUnitRepository courseUnitRepository,
                          CompetencyRepository competencyRepository,
                          AcademicHistoryService academicHistoryService,
                          CompetencyCatalogueService competencyCatalogueService,
                          ObjectProvider<SelfAssessmentProvider> selfAssessmentProvider) {
        this.studentRepository = studentRepository;
        this.courseUnitRepository = courseUnitRepository;
        this.competencyRepository = competencyRepository;
        this.academicHistoryService = academicHistoryService;
        this.competencyCatalogueService = competencyCatalogueService;
        this.selfAssessmentProvider = selfAssessmentProvider;
    }

    public ProfileView profileFor(long studentId) {
        Inputs inputs = inputsFor(studentId);
        StudentProfile profile = calculator.calculate(inputs.evidence(), inputs.weights(),
            inputs.selfAssessment(), inputs.competencies());
        return toView(profile, inputs.competencies());
    }

    /** #56: contribution of each course unit and of the self-assessment to one competency. */
    public ExplanationView explanationFor(long studentId, String competencyCode) {
        Inputs inputs = inputsFor(studentId);
        Competency competency = inputs.competencies().stream()
            .filter(c -> c.getCode().equals(competencyCode))
            .findFirst()
            .orElseThrow(() -> new CompetencyNotFoundException(competencyCode));

        CompetencyExplanation explanation = calculator.explain(competencyCode, inputs.evidence(), inputs.weights(),
            Optional.ofNullable(inputs.selfAssessment().get(competency.getArea().getCode())));
        CompetencyLevel level = explanation.level();
        return new ExplanationView(competencyCode, competency.getName(), value(level.level()),
            level.confidence().value(), value(level.evidenceLevel()), value(level.selfAssessedLevel()),
            explanation.selfAssessedShare(), explanation.contributions());
    }

    private record Inputs(List<EvidenceGrade> evidence, List<CourseUnitWeight> weights,
                          Map<String, Level> selfAssessment, List<Competency> competencies) {
    }

    private Inputs inputsFor(long studentId) {
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new StudentNotFoundException(studentId));

        List<String> programmeUnits = courseUnitRepository
            .findByDegreeProgramId(student.getDegreeProgram().getId()).stream()
            .map(CourseUnit::getCode)
            .toList();
        List<CourseUnitWeight> weights = programmeUnits.isEmpty()
            ? List.of()
            : competencyCatalogueService.weightsFor(programmeUnits);

        return new Inputs(academicHistoryService.evidenceGradesFor(studentId), weights,
            selfAssessmentFor(studentId), programmeCompetencies(weights));
    }

    /**
     * Only the areas with at least one weight in the student's programme appear (ADR-0003, section 1).
     * If the programme has no weights yet, the whole catalogue is shown so the profile is never empty.
     */
    private List<Competency> programmeCompetencies(List<CourseUnitWeight> weights) {
        List<Competency> catalogue = competencyRepository.findAllByOrderByIdAsc();
        Set<String> weightedCompetencies = weights.stream()
            .map(CourseUnitWeight::competencyCode)
            .collect(Collectors.toSet());
        if (weightedCompetencies.isEmpty()) {
            return catalogue;
        }
        Set<String> areasWithWeights = catalogue.stream()
            .filter(c -> weightedCompetencies.contains(c.getCode()))
            .map(c -> c.getArea().getCode())
            .collect(Collectors.toSet());
        return catalogue.stream()
            .filter(c -> areasWithWeights.contains(c.getArea().getCode()))
            .toList();
    }

    private Map<String, Level> selfAssessmentFor(long studentId) {
        SelfAssessmentProvider provider = selfAssessmentProvider.getIfAvailable();
        return provider == null ? Map.of() : provider.levelsByArea(studentId);
    }

    private static ProfileView toView(StudentProfile profile, List<Competency> competencies) {
        Map<String, Competency> byCode = competencies.stream()
            .collect(Collectors.toMap(Competency::getCode, Function.identity(), (a, b) -> a, LinkedHashMap::new));
        Map<String, String> areaNames = competencies.stream()
            .collect(Collectors.toMap(c -> c.getArea().getCode(), c -> c.getArea().getName(), (a, b) -> a));

        List<ProfileView.AreaView> areas = profile.areas().stream()
            .map(a -> new ProfileView.AreaView(a.areaCode(), areaNames.get(a.areaCode()),
                value(a.level()), a.confidence().value()))
            .toList();

        List<ProfileView.CompetencyView> competencyViews = profile.competencies().stream()
            .map(c -> {
                Competency competency = byCode.get(c.competencyCode());
                return new ProfileView.CompetencyView(c.competencyCode(), competency.getName(),
                    competency.getArea().getCode(), value(c.level()), c.confidence().value(),
                    value(c.evidenceLevel()), value(c.selfAssessedLevel()));
            })
            .toList();

        return new ProfileView(areas, competencyViews);
    }

    private static Double value(Level level) {
        return level == null ? null : level.value();
    }
}
