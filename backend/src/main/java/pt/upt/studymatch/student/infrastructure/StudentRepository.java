package pt.upt.studymatch.student.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.upt.studymatch.student.domain.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
}