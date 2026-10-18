-- 1. Insertar las Unidades Curriculares (Asignaturas) de LEI que faltan
INSERT INTO course_unit (code, name, curricular_year, semester, ects) VALUES
('PROG1', 'Programação I', 1, 1, 6),
('DB1', 'Bases de Dados I', 2, 1, 6);

-- 2. Añadir las Claves Foráneas (Foreign Keys) a la tabla de pesos existente
ALTER TABLE course_unit_competency ADD CONSTRAINT fk_cuc_course_unit FOREIGN KEY (course_unit_code) REFERENCES course_unit(code);
ALTER TABLE course_unit_competency ADD CONSTRAINT fk_cuc_competency FOREIGN KEY (competency_code) REFERENCES competency(code);

-- 3. Arreglar los estudiantes del V6 para que usen el ID correcto del grado LEI
UPDATE student 
SET degree_program_id = (SELECT id FROM degree_program WHERE code = 'LEI') 
WHERE number IN ('DEMO-A-3', 'DEMO-B-1');

-- 4. Crear historial para el Estudiante A (Demo Veterano) con año '2024/25'
-- Insertar Matrículas (Enrolment)
INSERT INTO enrolment (student_id, course_unit_code, academic_year, status) VALUES 
((SELECT id FROM student WHERE number = 'DEMO-A-3'), 'PROG1', '2024/25', 'PASSED'),
((SELECT id FROM student WHERE number = 'DEMO-A-3'), 'DB1', '2024/25', 'PASSED');

-- Insertar Intentos (Attempts) simulando una mejora de nota
INSERT INTO attempt (enrolment_id, season, grade, date) VALUES 
((SELECT id FROM enrolment WHERE course_unit_code = 'PROG1' AND student_id = (SELECT id FROM student WHERE number = 'DEMO-A-3')), 'NORMAL', 12, '2024-12-15'),
((SELECT id FROM enrolment WHERE course_unit_code = 'PROG1' AND student_id = (SELECT id FROM student WHERE number = 'DEMO-A-3')), 'MELHORIA', 16, '2025-01-20'),
((SELECT id FROM enrolment WHERE course_unit_code = 'DB1' AND student_id = (SELECT id FROM student WHERE number = 'DEMO-A-3')), 'NORMAL', 14, '2025-01-18');