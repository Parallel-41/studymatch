package pt.upt.studymatch.academic.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.upt.studymatch.academic.domain.DegreeProgram;

import java.util.Optional;

public interface DegreeProgramRepository extends JpaRepository<DegreeProgram, Long> {

    Optional<DegreeProgram> findByCode(String code);
}
