
package pt.upt.studymatch.academic.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.upt.studymatch.academic.domain.CourseUnit;

import java.util.Optional;

public interface CourseUnitRepository extends JpaRepository<CourseUnit, Long> {

    Optional<CourseUnit> findByCode(String code);
}
