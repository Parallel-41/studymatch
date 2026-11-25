package pt.upt.studymatch.grouping.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.upt.studymatch.grouping.domain.GroupingContext;

public interface GroupingContextRepository extends JpaRepository<GroupingContext, Long> {
}
