CREATE TABLE competency_area (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE competency (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    area_id BIGINT NOT NULL,
    CONSTRAINT fk_competency_area FOREIGN KEY (area_id) REFERENCES competency_area (id)
);

INSERT INTO competency_area (code, name) VALUES
    ('PROG', 'Programming'),
    ('DB', 'Databases'),
    ('WEB', 'Web Development'),
    ('QUAL', 'Software Quality'),
    ('MATH', 'Mathematics & Logic'),
    ('SOFT', 'Soft Skills & Teamwork');

INSERT INTO competency (code, name, area_id) VALUES
    ('PROG_OO', 'Object-Oriented Programming', (SELECT id FROM competency_area WHERE code = 'PROG')),
    ('PROG_ALG', 'Algorithms & Data Structures', (SELECT id FROM competency_area WHERE code = 'PROG')),
    ('PROG_ARCH', 'Software Architecture', (SELECT id FROM competency_area WHERE code = 'PROG')),
    ('DB_SQL', 'Relational Databases & SQL', (SELECT id FROM competency_area WHERE code = 'DB')),
    ('DB_MODEL', 'Data Modelling', (SELECT id FROM competency_area WHERE code = 'DB')),
    ('WEB_FE', 'Frontend Development', (SELECT id FROM competency_area WHERE code = 'WEB')),
    ('WEB_BE', 'Backend & REST APIs', (SELECT id FROM competency_area WHERE code = 'WEB')),
    ('QUAL_TEST', 'Automated Testing', (SELECT id FROM competency_area WHERE code = 'QUAL')),
    ('QUAL_CI', 'CI/CD & Version Control', (SELECT id FROM competency_area WHERE code = 'QUAL')),
    ('QUAL_REQ', 'Requirements & Use Cases', (SELECT id FROM competency_area WHERE code = 'QUAL')),
    ('MATH_DISC', 'Discrete Mathematics', (SELECT id FROM competency_area WHERE code = 'MATH')),
    ('MATH_STAT', 'Statistics & Probability', (SELECT id FROM competency_area WHERE code = 'MATH')),
    ('SOFT_COMM', 'Technical Communication', (SELECT id FROM competency_area WHERE code = 'SOFT')),
    ('SOFT_TEAM', 'Team Collaboration', (SELECT id FROM competency_area WHERE code = 'SOFT')),
    ('SOFT_LEAD', 'Project Organization', (SELECT id FROM competency_area WHERE code = 'SOFT'));