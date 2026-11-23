-- 1. Insertar las Unidades Curriculares (Asignaturas) de LEI que faltan
INSERT INTO course_unit (code, name, curricular_year, semester, ects, degree_program_id)
SELECT 'PROG1', 'Programação I', 1, 1, 6, id FROM degree_program WHERE code = 'LEI'
UNION ALL
SELECT 'DB1', 'Bases de Dados I', 2, 1, 6, id FROM degree_program WHERE code = 'LEI';

-- 2. Añadir las Claves Foráneas (Foreign Keys) a la tabla de pesos existente
ALTER TABLE course_unit_competency ADD CONSTRAINT fk_cuc_course_unit FOREIGN KEY (course_unit_code) REFERENCES course_unit(code);
ALTER TABLE course_unit_competency ADD CONSTRAINT fk_cuc_competency FOREIGN KEY (competency_code) REFERENCES competency(code);

-- 3. Arreglar los estudiantes del V6 para que usen el ID correcto del grado LEI
UPDATE student 
SET degree_program_id = (SELECT id FROM degree_program WHERE code = 'LEI') 
WHERE number IN ('DEMO-A-3', 'DEMO-B-1');

-- 4. Crear historial para el Estudiante A (Demo Veterano) con año '2024/25'
-- Insertar Matrículas (Enrolment)
INSERT INTO enrolment (student_id, course_unit_id, academic_year, status)
SELECT s.id, cu.id, '2024/25', 'PASSED'
FROM student s JOIN course_unit cu ON cu.code IN ('PROG1', 'DB1')
WHERE s.number = 'DEMO-A-3';

-- Insertar Intentos (Attempts) simulando una mejora de nota
INSERT INTO attempt (enrolment_id, season, grade, date)
SELECT e.id, 'NORMAL', 12, '2025-01-20' FROM enrolment e
  JOIN course_unit cu ON cu.id = e.course_unit_id JOIN student s ON s.id = e.student_id
  WHERE s.number = 'DEMO-A-3' AND cu.code = 'PROG1'
UNION ALL
SELECT e.id, 'MELHORIA', 16, '2025-07-15' FROM enrolment e
  JOIN course_unit cu ON cu.id = e.course_unit_id JOIN student s ON s.id = e.student_id
  WHERE s.number = 'DEMO-A-3' AND cu.code = 'PROG1'
UNION ALL
SELECT e.id, 'NORMAL', 14, '2025-01-18' FROM enrolment e
  JOIN course_unit cu ON cu.id = e.course_unit_id JOIN student s ON s.id = e.student_id
  WHERE s.number = 'DEMO-A-3' AND cu.code = 'DB1';