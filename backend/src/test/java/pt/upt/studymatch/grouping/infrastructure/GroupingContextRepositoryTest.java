package pt.upt.studymatch.grouping.infrastructure;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pt.upt.studymatch.grouping.domain.GroupingContext;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Runs V9 with the real Flyway migrations on MySQL and checks the participant constraints. */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class GroupingContextRepositoryTest {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
        .withDatabaseName("studymatch_test")
        .withUsername("studymatch")
        .withPassword("studymatch-test-password");

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-23T10:00:00Z"), ZoneOffset.UTC);

    @Autowired
    private GroupingContextRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void registerMySqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    private List<Long> seededStudentIds() {
        return jdbcTemplate.queryForList("SELECT id FROM student ORDER BY id LIMIT 3", Long.class);
    }

    @Test
    void persistsContextWithParticipants() {
        List<Long> studentIds = seededStudentIds();
        GroupingContext context = new GroupingContext("Software project teams", "Semester project", null, CLOCK);
        context.replaceParticipants(studentIds);

        Long id = repository.saveAndFlush(context).getId();
        entityManager.clear();

        GroupingContext reloaded = repository.findById(id).orElseThrow();
        assertEquals("Software project teams", reloaded.getName());
        assertEquals(Instant.parse("2026-11-23T10:00:00Z"), reloaded.getCreatedAt());
        assertEquals(studentIds, reloaded.getStudentIds());
    }

    @Test
    void databaseRejectsTheSameStudentTwiceInOneContext() {
        Long studentId = seededStudentIds().get(0);
        GroupingContext context = new GroupingContext("Duplicate check", null, null, CLOCK);
        context.replaceParticipants(List.of(studentId));
        Long contextId = repository.saveAndFlush(context).getId();

        assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.update(
            "INSERT INTO grouping_participant (context_id, student_id) VALUES (?, ?)", contextId, studentId
        ));
    }

    @Test
    void databaseRejectsAnUnknownStudent() {
        GroupingContext context = new GroupingContext("Unknown student", null, null, CLOCK);
        context.replaceParticipants(List.of(999_999L));

        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(context));
    }
}
