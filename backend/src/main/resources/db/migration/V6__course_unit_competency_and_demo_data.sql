CREATE TABLE course_unit_competency (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_unit_code VARCHAR(50) NOT NULL,
    competency_code VARCHAR(30) NOT NULL,
    weight DOUBLE NOT NULL,
    CONSTRAINT chk_weight CHECK (weight > 0.0 AND weight <= 1.0)
);

-- Pesos ficticios para algunas asignaturas
INSERT INTO course_unit_competency (course_unit_code, competency_code, weight) VALUES
('PROG1', 'PROG_OO', 0.6),
('PROG1', 'PROG_ALG', 0.4),
('DB1', 'DB_SQL', 0.8),
('DB1', 'DB_MODEL', 0.2);

-- Estudiantes de demostración para la Review del Sprint 2
-- Estudiante A: 3.º curso (historial completo simulado)
INSERT INTO student (number, name, email, registration_year, degree_program_id) 
VALUES ('DEMO-A-3', 'Estudiante Veterano A', 'demo_a@upt.pt', 2024, 1);

-- Estudiante B: 1.º curso (cold start, sin historial)
INSERT INTO student (number, name, email, registration_year, degree_program_id) 
VALUES ('DEMO-B-1', 'Estudiante Novato B', 'demo_b@upt.pt', 2026, 1);