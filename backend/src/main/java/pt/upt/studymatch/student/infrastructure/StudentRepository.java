package pt.upt.studymatch.student.infrastructure;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pt.upt.studymatch.student.domain.Student;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByNumber(String number);

    boolean existsByEmail(String email);

    boolean existsByNumberAndIdNot(String number, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Override
    @EntityGraph(attributePaths = "degreeProgram")
    List<Student> findAll();

    @Override
    @EntityGraph(attributePaths = "degreeProgram")
    Optional<Student> findById(Long id);

    @EntityGraph(attributePaths = "degreeProgram")
    List<Student> findByNameContainingIgnoreCaseOrNumberContainingIgnoreCase(String nameQuery, String numberQuery);
}