package pt.upt.studymatch.academic.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.upt.studymatch.academic.domain.CourseUnit;
import pt.upt.studymatch.academic.domain.Enrolment;
import pt.upt.studymatch.academic.domain.EvidenceGrade;
import pt.upt.studymatch.academic.infrastructure.EnrolmentRepository;
import pt.upt.studymatch.common.domain.Grade;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AcademicHistoryService {

    private final EnrolmentRepository enrolmentRepository;
    private final Clock clock;

    public AcademicHistoryService(
        EnrolmentRepository enrolmentRepository,
        Clock clock) {
        this.enrolmentRepository = enrolmentRepository;
        this.clock = clock;
    }

    public List<EvidenceGrade> evidenceGradesFor(Long studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("studentId must not be null.");
        }

        LocalDate today = LocalDate.now(clock);
        int currentSemester = currentSemesterIndex(today);
        Map<String, EvidenceGrade> evidenceByCourseUnit = new HashMap<>();

        for (Enrolment enrolment : enrolmentRepository.findByStudentId(studentId)) {
            Optional<Grade> bestGrade = enrolment.bestPassingGrade();
            if (bestGrade.isEmpty()) {
                continue;
            }

            CourseUnit courseUnit = enrolment.getCourseUnit();
            int academicYearStart = parseAcademicYearStart(enrolment.getAcademicYear());
            int courseSemester = courseUnit.getSemester();
            if (courseSemester != 1 && courseSemester != 2) {
                throw new IllegalArgumentException("Semester must be 1 or 2.");
            }

            int courseSemesterIndex = academicYearStart * 2 + courseSemester - 1;
            int semestersAgo = Math.max(0, currentSemester - courseSemesterIndex);

            EvidenceGrade candidate = new EvidenceGrade(
                courseUnit.getCode(),
                courseUnit.getEcts(),
                bestGrade.get(),
                semestersAgo
            );

            EvidenceGrade previous = evidenceByCourseUnit.get(candidate.courseUnitCode());
            if (previous == null
                || candidate.grade().value() > previous.grade().value()
                || (candidate.grade().value() == previous.grade().value()
                    && candidate.semestersAgo() < previous.semestersAgo())) {
                evidenceByCourseUnit.put(candidate.courseUnitCode(), candidate);
            }
        }

        List<EvidenceGrade> evidence = new ArrayList<>(evidenceByCourseUnit.values());
        evidence.sort(Comparator.comparingInt(EvidenceGrade::semestersAgo)
            .thenComparing(EvidenceGrade::courseUnitCode));
        return List.copyOf(evidence);
    }

    private static int currentSemesterIndex(LocalDate date) {
        int month = date.getMonthValue();
        int academicYearStart;
        int semester;

        if (month >= 9) {
            academicYearStart = date.getYear();
            semester = 1;
        } else if (month >= 2) {
            academicYearStart = date.getYear() - 1;
            semester = 2;
        } else {
            academicYearStart = date.getYear() - 1;
            semester = 1;
        }

        return academicYearStart * 2 + semester - 1;
    }

    private static int parseAcademicYearStart(String academicYear) {
        if (academicYear == null || !academicYear.matches("\\d{4}/\\d{2}")) {
            throw new IllegalArgumentException(
                "Academic year must have the format YYYY/YY, e.g. 2024/25."
            );
        }

        int start = Integer.parseInt(academicYear.substring(0, 4));
        int end = Integer.parseInt(academicYear.substring(5, 7));
        if (end != (start + 1) % 100) {
            throw new IllegalArgumentException(
                "Academic year must span consecutive years."
            );
        }
        return start;
    }
}
