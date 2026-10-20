
package pt.upt.studymatch.academic.infrastructure;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pt.upt.studymatch.academic.domain.Enrolment;

import java.util.List;
import java.util.Optional;

public interface EnrolmentRepository
    extends JpaRepository<Enrolment, Long> {

    List<Enrolment> findByStudentId(Long studentId);

    @EntityGraph(attributePaths = {"courseUnit", "attempts"})
    List<Enrolment> findAllByStudentId(Long studentId);

    @EntityGraph(attributePaths = {"courseUnit", "attempts"})
    Optional<Enrolment> findByStudentIdAndCourseUnitIdAndAcademicYear(Long studentId, Long courseUnitId, String academicYear);
}
