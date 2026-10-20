package pt.upt.studymatch.profile.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** #49: the profile is computed on every request, so a new grade changes it on the next call. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class ProfileApiIntegrationTest {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
        .withDatabaseName("studymatch_test")
        .withUsername("studymatch")
        .withPassword("studymatch-test-password");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbc;

    @DynamicPropertySource
    static void registerMySqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Test
    void addingABetterGradeChangesTheProfileOnTheNextRequest() {
        long programme = jdbc.queryForObject("SELECT id FROM degree_program WHERE code = 'LEI'", Long.class);
        jdbc.update("INSERT INTO student (number, name, email, registration_year, degree_program_id) "
            + "VALUES ('IT-PROFILE-49', 'Profile IT', 'profile49@example.com', 2024, ?)", programme);
        long studentId = jdbc.queryForObject("SELECT id FROM student WHERE number = 'IT-PROFILE-49'", Long.class);
        jdbc.update("INSERT INTO course_unit (code, name, curricular_year, semester, ects, degree_program_id) "
            + "VALUES ('IT-OOP', 'Integration OOP', 1, 1, 6, ?)", programme);
        long unitId = jdbc.queryForObject("SELECT id FROM course_unit WHERE code = 'IT-OOP'", Long.class);
        jdbc.update("INSERT INTO course_unit_competency (course_unit_code, competency_code, weight) "
            + "VALUES ('IT-OOP', 'PROG_OO', 1.0)");
        jdbc.update("INSERT INTO enrolment (student_id, course_unit_id, academic_year, status) "
            + "VALUES (?, ?, '2024/25', 'PASSED')", studentId, unitId);
        long enrolmentId = jdbc.queryForObject("SELECT id FROM enrolment WHERE student_id = ?", Long.class, studentId);
        jdbc.update("INSERT INTO attempt (enrolment_id, season, grade, date) VALUES (?, 'NORMAL', 12, '2025-01-20')",
            enrolmentId);

        double before = oopLevel(studentId);

        jdbc.update("INSERT INTO attempt (enrolment_id, season, grade, date) VALUES (?, 'MELHORIA', 18, '2025-07-15')",
            enrolmentId);

        double after = oopLevel(studentId);
        assertThat(after).isGreaterThan(before);
    }

    @Test
    void unknownStudentReturns404() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/students/999999/profile", String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    private double oopLevel(long studentId) {
        ResponseEntity<ProfileResponse> response =
            restTemplate.getForEntity("/api/students/" + studentId + "/profile", ProfileResponse.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        return response.getBody().competencies().stream()
            .filter(c -> c.code().equals("PROG_OO"))
            .findFirst().orElseThrow()
            .evidenceLevel();
    }
}
