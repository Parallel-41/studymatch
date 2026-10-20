package pt.upt.studymatch.academic.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pt.upt.studymatch.academic.domain.CourseUnit;

public interface CourseUnitRepository extends JpaRepository<CourseUnit, Long> {

    Optional<CourseUnit> findByCode(String code);

    List<CourseUnit> findByDegreeProgramId(Long degreeProgramId);
}
