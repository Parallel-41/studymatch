package pt.upt.studymatch.academic.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.upt.studymatch.academic.domain.DegreeProgram;

public interface DegreeProgramRepository extends JpaRepository<DegreeProgram, Long> {
}
