package pt.upt.studymatch.student.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Exercises the HTTP API against MySQL with the real Flyway migrations and seed data. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class StudentApiIntegrationTest {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
        .withDatabaseName("studymatch_test")
        .withUsername("studymatch")
        .withPassword("studymatch-test-password");

    @Autowired
    private TestRestTemplate restTemplate;

    @DynamicPropertySource
    static void registerMySqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Test
    void flywayMigrationsAndSampleStudentsAreAvailableThroughApi() {
        ResponseEntity<StudentResponse[]> response = restTemplate.getForEntity(
            "/api/students",
            StudentResponse[].class
        );

        assertEquals(200, response.getStatusCode().value());
        StudentResponse[] students = response.getBody();
        assertNotNull(students);
        assertTrue(students.length >= 8, "Expected V2 and V6 sample students to be present");
        assertTrue(java.util.Arrays.stream(students)
            .anyMatch(student -> "DEMO-B-1".equals(student.number())));
        assertTrue(java.util.Arrays.stream(students)
            .allMatch(student -> student.degreeProgram() != null
                && "LEI".equals(student.degreeProgram().code())));

        StudentRequest request = new StudentRequest(
            "IT-API-37",
            "Integration Test Student",
            "issue37-api@example.com",
            2026,
            "LEI"
        );
        ResponseEntity<StudentResponse> created = restTemplate.postForEntity(
            "/api/students", request, StudentResponse.class
        );
        assertEquals(201, created.getStatusCode().value());
        assertNotNull(created.getBody());
        assertNotNull(created.getBody().id());
        assertEquals("IT-API-37", created.getBody().number());
    }
}
