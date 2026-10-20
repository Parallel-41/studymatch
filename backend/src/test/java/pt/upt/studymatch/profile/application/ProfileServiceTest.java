package pt.upt.studymatch.profile.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import pt.upt.studymatch.academic.application.AcademicHistoryService;
import pt.upt.studymatch.academic.domain.CourseUnit;
import pt.upt.studymatch.academic.domain.DegreeProgram;
import pt.upt.studymatch.academic.domain.EvidenceGrade;
import pt.upt.studymatch.academic.infrastructure.CourseUnitRepository;
import pt.upt.studymatch.common.domain.Grade;
import pt.upt.studymatch.common.domain.Level;
import pt.upt.studymatch.competency.application.CompetencyCatalogueService;
import pt.upt.studymatch.competency.domain.Competency;
import pt.upt.studymatch.competency.domain.CompetencyArea;
import pt.upt.studymatch.competency.domain.CourseUnitWeight;
import pt.upt.studymatch.competency.infrastructure.CompetencyRepository;
import pt.upt.studymatch.student.application.StudentNotFoundException;
import pt.upt.studymatch.student.domain.Student;
import pt.upt.studymatch.student.infrastructure.StudentRepository;

class ProfileServiceTest {

    private static final CompetencyArea PROG = new CompetencyArea("PROG", "Programming");
    private static final CompetencyArea DB = new CompetencyArea("DB", "Databases");
    private static final Competency OOP = new Competency("PROG_OO", "Object-Oriented Programming", PROG);
    private static final Competency ALG = new Competency("PROG_ALG", "Algorithms & Data Structures", PROG);
    private static final Competency SQL = new Competency("DB_SQL", "Relational Databases & SQL", DB);

    private StudentRepository studentRepository;
    private CourseUnitRepository courseUnitRepository;
    private CompetencyRepository competencyRepository;
    private AcademicHistoryService academicHistoryService;
    private CompetencyCatalogueService catalogueService;
    private ObjectProvider<SelfAssessmentProvider> selfAssessment;
    private ProfileService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        studentRepository = mock(StudentRepository.class);
        courseUnitRepository = mock(CourseUnitRepository.class);
        competencyRepository = mock(CompetencyRepository.class);
        academicHistoryService = mock(AcademicHistoryService.class);
        catalogueService = mock(CompetencyCatalogueService.class);
        selfAssessment = mock(ObjectProvider.class);
        service = new ProfileService(studentRepository, courseUnitRepository, competencyRepository,
            academicHistoryService, catalogueService, selfAssessment);

        Student student = new Student("2024001", "Ana Silva", "ana@example.com", 2024,
            new DegreeProgram("LEI", "Licenciatura em Engenharia Informática"));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        CourseUnit prog1 = mock(CourseUnit.class);
        when(prog1.getCode()).thenReturn("PROG1");
        when(courseUnitRepository.findByDegreeProgramId(any())).thenReturn(List.of(prog1));
        when(catalogueService.weightsFor(anyCollection()))
            .thenReturn(List.of(new CourseUnitWeight("PROG1", "PROG_OO", 1.0)));
        when(competencyRepository.findAllByOrderByIdAsc()).thenReturn(List.of(OOP, ALG, SQL));
        when(academicHistoryService.evidenceGradesFor(1L))
            .thenReturn(List.of(new EvidenceGrade("PROG1", 6, new Grade(14), 0)));
    }

    @Test
    void unknownStudentThrowsNotFound() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.profileFor(99L)).isInstanceOf(StudentNotFoundException.class);
    }

    @Test
    void computesLevelsFromEvidenceWithCatalogueNames() {
        ProfileView profile = service.profileFor(1L);

        ProfileView.CompetencyView oop = profile.competencies().get(0);
        assertThat(oop.code()).isEqualTo("PROG_OO");
        assertThat(oop.name()).isEqualTo("Object-Oriented Programming");
        assertThat(oop.area()).isEqualTo("PROG");
        assertThat(oop.level()).isCloseTo(70.0, within(1e-9));   // grade 14 → 70
        assertThat(oop.confidence()).isCloseTo(0.5, within(1e-9)); // 6 of 12 ECTS
    }

    @Test
    void showsOnlyAreasWithWeightsInTheProgramme() {
        ProfileView profile = service.profileFor(1L);

        assertThat(profile.areas()).extracting(ProfileView.AreaView::code).containsExactly("PROG");
        assertThat(profile.competencies()).extracting(ProfileView.CompetencyView::code)
            .containsExactly("PROG_OO", "PROG_ALG");
    }

    @Test
    void showsWholeCatalogueWhenProgrammeHasNoWeightsYet() {
        when(courseUnitRepository.findByDegreeProgramId(any())).thenReturn(List.of());

        ProfileView profile = service.profileFor(1L);

        assertThat(profile.areas()).extracting(ProfileView.AreaView::code).containsExactly("PROG", "DB");
        assertThat(profile.competencies()).allSatisfy(c -> assertThat(c.level()).isNull());
    }

    @Test
    void withoutSelfAssessmentProviderUsesEvidenceOnly() {
        when(selfAssessment.getIfAvailable()).thenReturn(null);

        ProfileView profile = service.profileFor(1L);

        assertThat(profile.competencies()).allSatisfy(c -> assertThat(c.selfAssessedLevel()).isNull());
        assertThat(profile.competencies().get(1).level()).isNull(); // PROG_ALG: no evidence, no self-assessment
    }

    @Test
    void usesSelfAssessmentWhenProviderExists() {
        when(selfAssessment.getIfAvailable()).thenReturn(id -> Map.of("PROG", new Level(40)));

        ProfileView profile = service.profileFor(1L);

        ProfileView.CompetencyView alg = profile.competencies().get(1);
        assertThat(alg.level()).isEqualTo(40.0);
        assertThat(alg.confidence()).isZero();
        assertThat(profile.competencies().get(0).level()).isCloseTo(0.5 * 70 + 0.5 * 40, within(1e-9));
    }

    @Test
    void explanationListsTheContributionOfEachCourseUnit() {
        ExplanationView explanation = service.explanationFor(1L, "PROG_OO");

        assertThat(explanation.competencyName()).isEqualTo("Object-Oriented Programming");
        assertThat(explanation.level()).isCloseTo(70.0, within(1e-9));
        assertThat(explanation.contributions()).hasSize(1);
        assertThat(explanation.contributions().get(0).courseUnitCode()).isEqualTo("PROG1");
        assertThat(explanation.contributions().get(0).points()).isCloseTo(70.0, within(1e-9));
        assertThat(explanation.contributions().get(0).share()).isCloseTo(1.0, within(1e-9));
    }

    @Test
    void explanationOfCompetencyOutsideTheProgrammeThrowsNotFound() {
        assertThatThrownBy(() -> service.explanationFor(1L, "DB_SQL"))
            .isInstanceOf(CompetencyNotFoundException.class);
        assertThatThrownBy(() -> service.explanationFor(1L, "UNKNOWN"))
            .isInstanceOf(CompetencyNotFoundException.class);
    }
}
