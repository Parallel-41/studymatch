package pt.upt.studymatch.competency.infrastructure;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pt.upt.studymatch.competency.domain.CourseUnitCompetency;

public interface CourseUnitCompetencyRepository extends JpaRepository<CourseUnitCompetency, Long> {

    List<CourseUnitCompetency> findByCourseUnitCodeIn(Collection<String> courseUnitCodes);
}