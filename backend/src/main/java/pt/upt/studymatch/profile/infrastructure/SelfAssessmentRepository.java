package pt.upt.studymatch.profile.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.upt.studymatch.profile.domain.SelfAssessment;

import java.util.List;
import java.util.Optional;

@Repository
public interface SelfAssessmentRepository extends JpaRepository<SelfAssessment, Long> {

    List<SelfAssessment> findAllByStudentId(Long studentId);

    Optional<SelfAssessment> findByStudentIdAndAreaCode(Long studentId, String areaCode);
}
