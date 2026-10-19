package pt.upt.studymatch.academic.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pt.upt.studymatch.academic.domain.CourseUnit;

public interface CourseUnitRepository extends JpaRepository<CourseUnit, Long> {

    List<CourseUnit> findByDegreeProgramId(Long degreeProgramId);
}
