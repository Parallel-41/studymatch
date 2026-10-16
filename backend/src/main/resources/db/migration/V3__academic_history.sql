CREATE TABLE degree_program (
                                id BIGINT NOT NULL AUTO_INCREMENT,
                                code VARCHAR(20) NOT NULL,
                                name VARCHAR(255) NOT NULL,

                                CONSTRAINT pk_degree_program PRIMARY KEY (id),
                                CONSTRAINT uk_degree_program_code UNIQUE (code)
);

INSERT INTO degree_program (code, name)
VALUES ('LEI', 'Licenciatura em Engenharia Informática');

CREATE TABLE course_unit (
                             id BIGINT NOT NULL AUTO_INCREMENT,
                             code VARCHAR(20) NOT NULL,
                             name VARCHAR(255) NOT NULL,
                             curricular_year INT NOT NULL,
                             semester INT NOT NULL,
                             ects INT NOT NULL,
                             degree_program_id BIGINT NOT NULL,

                             CONSTRAINT pk_course_unit PRIMARY KEY (id),
                             CONSTRAINT uk_course_unit_code UNIQUE (code),

                             CONSTRAINT fk_course_unit_degree_program
                                 FOREIGN KEY (degree_program_id)
                                     REFERENCES degree_program(id),

                             CONSTRAINT chk_course_unit_curricular_year
                                 CHECK (curricular_year BETWEEN 1 AND 5),

                             CONSTRAINT chk_course_unit_semester
                                 CHECK (semester BETWEEN 1 AND 2),

                             CONSTRAINT chk_course_unit_ects
                                 CHECK (ects > 0)
);
CREATE TABLE enrolment (
                           id BIGINT NOT NULL AUTO_INCREMENT,
                           student_id BIGINT NOT NULL,
                           course_unit_id BIGINT NOT NULL,
                           academic_year VARCHAR(7) NOT NULL,
                           status VARCHAR(20) NOT NULL,

                           CONSTRAINT pk_enrolment PRIMARY KEY (id),

                           CONSTRAINT fk_enrolment_student
                               FOREIGN KEY (student_id)
                                   REFERENCES student(id),

                           CONSTRAINT fk_enrolment_course_unit
                               FOREIGN KEY (course_unit_id)
                                   REFERENCES course_unit(id),

                           CONSTRAINT chk_enrolment_status
                               CHECK (status IN ('ENROLLED', 'PASSED', 'FAILED'))
);
CREATE TABLE attempt (
                         id BIGINT NOT NULL AUTO_INCREMENT,
                         enrolment_id BIGINT NOT NULL,
                         season VARCHAR(20) NOT NULL,
                         grade DECIMAL(4,2),
                         date DATE NOT NULL,

                         CONSTRAINT pk_attempt PRIMARY KEY (id),

                         CONSTRAINT fk_attempt_enrolment
                             FOREIGN KEY (enrolment_id)
                                 REFERENCES enrolment(id),

                         CONSTRAINT chk_attempt_season
                             CHECK (season IN ('NORMAL', 'RECURSO', 'ESPECIAL', 'MELHORIA')),

                         CONSTRAINT chk_attempt_grade
                             CHECK (grade IS NULL OR (grade BETWEEN 0 AND 20))
);
ALTER TABLE student
    ADD COLUMN registration_year INT;

UPDATE student
SET registration_year = 2024;

ALTER TABLE student
    ADD COLUMN degree_program_id BIGINT;

UPDATE student
SET degree_program_id = (
    SELECT id
    FROM degree_program
    WHERE code = 'LEI'
);

ALTER TABLE student
    MODIFY COLUMN registration_year INT NOT NULL,
    MODIFY COLUMN degree_program_id BIGINT NOT NULL;

ALTER TABLE student
    ADD CONSTRAINT fk_student_degree_program
        FOREIGN KEY (degree_program_id)
            REFERENCES degree_program(id);

ALTER TABLE student
DROP CHECK chk_student_current_year;

ALTER TABLE student
DROP COLUMN current_year;
