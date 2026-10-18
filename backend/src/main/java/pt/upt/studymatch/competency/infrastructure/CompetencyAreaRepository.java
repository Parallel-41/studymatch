package pt.upt.studymatch.competency.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pt.upt.studymatch.competency.domain.CompetencyArea;

public interface CompetencyAreaRepository extends JpaRepository<CompetencyArea, Long> {

    List<CompetencyArea> findAllByOrderByIdAsc();
}