package pt.upt.studymatch.competency.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pt.upt.studymatch.competency.domain.Competency;

public interface CompetencyRepository extends JpaRepository<Competency, Long> {

    List<Competency> findAllByOrderByIdAsc();
}