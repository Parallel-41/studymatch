package pt.upt.studymatch.academic.application;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.academic.api.AttemptRequest;
import pt.upt.studymatch.academic.api.CourseUnitResponse;
import pt.upt.studymatch.academic.api.HistoryResponse;
import pt.upt.studymatch.academic.domain.AssessmentSeason;
import pt.upt.studymatch.academic.domain.Attempt;
import pt.upt.studymatch.academic.domain.CourseUnit;
import pt.upt.studymatch.academic.domain.Enrolment;
import pt.upt.studymatch.academic.domain.EnrolmentStatus;
import pt.upt.studymatch.academic.infrastructure.CourseUnitRepository;
import pt.upt.studymatch.academic.infrastructure.EnrolmentRepository;
import pt.upt.studymatch.common.domain.Grade;
import pt.upt.studymatch.student.application.StudentNotFoundException;
import pt.upt.studymatch.student.domain.Student;
import pt.upt.studymatch.student.infrastructure.StudentRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class AcademicHistoryApiService {
    private final StudentRepository studentRepository;
    private final CourseUnitRepository courseUnitRepository;
    private final EnrolmentRepository enrolmentRepository;
    private final Clock clock;

    public AcademicHistoryApiService(StudentRepository studentRepository, CourseUnitRepository courseUnitRepository,
                                     EnrolmentRepository enrolmentRepository, Clock clock) {
        this.studentRepository = studentRepository;
        this.courseUnitRepository = courseUnitRepository;
        this.enrolmentRepository = enrolmentRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public HistoryResponse history(Long studentId) {
        requireStudent(studentId);
        Map<String, Map<Integer, List<HistoryResponse.Unit>>> years = new TreeMap<>(Comparator.reverseOrder());
        for (Enrolment enrolment : enrolmentRepository.findAllByStudentId(studentId)) {
            CourseUnit unit = enrolment.getCourseUnit();
            List<HistoryResponse.Attempt> attempts = enrolment.getAttempts().stream()
                .sorted(Comparator.comparing(Attempt::getDate).thenComparing(a -> a.getSeason().name()))
                .map(a -> new HistoryResponse.Attempt(a.getSeason().name(), a.getGrade() == null ? null : a.getGrade().value()))
                .toList();
            Integer bestGrade = enrolment.bestPassingGrade()
                .map(Grade::value)
                .orElse(null);
            HistoryResponse.Unit responseUnit = new HistoryResponse.Unit(unit.getCode(), unit.getName(), unit.getEcts(), attempts, bestGrade);
            years.computeIfAbsent(enrolment.getAcademicYear(), ignored -> new TreeMap<>())
                .computeIfAbsent(unit.getSemester(), ignored -> new java.util.ArrayList<>()).add(responseUnit);
        }
        List<HistoryResponse.Year> responseYears = years.entrySet().stream().map(year ->
            new HistoryResponse.Year(year.getKey(), year.getValue().entrySet().stream().map(semester ->
                new HistoryResponse.Semester(semester.getKey(), semester.getValue().stream()
                    .sorted(Comparator.comparing(HistoryResponse.Unit::code)).toList())
            ).toList())
        ).toList();
        return new HistoryResponse(responseYears);
    }

    @Transactional
    public void addAttempt(Long studentId, AttemptRequest request) {
        Student student = requireStudent(studentId);
        CourseUnit unit = courseUnitRepository.findByCode(request.courseUnitCode())
            .orElseThrow(() -> new IllegalArgumentException("Unknown course unit: " + request.courseUnitCode()));
        validateAcademicYear(request.academicYear());
        Enrolment enrolment = enrolmentRepository.findByStudentIdAndCourseUnitIdAndAcademicYear(studentId, unit.getId(), request.academicYear())
            .orElseGet(() -> new Enrolment(student, unit, request.academicYear(), EnrolmentStatus.ENROLLED));
        boolean duplicateSeason = enrolment.getAttempts().stream().anyMatch(a -> a.getSeason() == request.season());
        if (duplicateSeason) {
            throw new DataIntegrityViolationException("An attempt for this season already exists.");
        }
        enrolment.addAttempt(new Attempt(enrolment, request.season(), new Grade(request.grade()), LocalDate.now(clock)));
        enrolmentRepository.save(enrolment);
    }

    @Transactional(readOnly = true)
    public List<CourseUnitResponse> courseUnits() {
        return courseUnitRepository.findAll().stream().sorted(Comparator.comparing(CourseUnit::getCode))
            .map(CourseUnitResponse::from).toList();
    }

    private Student requireStudent(Long studentId) {
        return studentRepository.findById(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
    }

    private static void validateAcademicYear(String year) {
        if (year == null || !year.matches("\\d{4}/\\d{2}")) throw new IllegalArgumentException("Academic year must have format YYYY/YY.");
        int start = Integer.parseInt(year.substring(0, 4));
        if (Integer.parseInt(year.substring(5, 7)) != (start + 1) % 100) throw new IllegalArgumentException("Academic year must span consecutive years.");
    }
}
