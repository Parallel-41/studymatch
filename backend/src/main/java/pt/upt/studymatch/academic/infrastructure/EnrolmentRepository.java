
package pt.upt.studymatch.academic.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.upt.studymatch.academic.domain.Enrolment;

import java.util.List;

public interface EnrolmentRepository
    extends JpaRepository<Enrolment, Long> {

    List<Enrolment> findByStudentId(Long studentId);
}
