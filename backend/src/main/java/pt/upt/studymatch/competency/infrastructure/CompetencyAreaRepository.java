package pt.upt.studymatch.competency.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.upt.studymatch.competency.domain.CompetencyArea;
import java.util.List;

public interface CompetencyAreaRepository extends JpaRepository<CompetencyArea, Long> {

    boolean existsByCode(String code);

    List<CompetencyArea> findAllByOrderByIdAsc();

}
