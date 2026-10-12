CREATE TABLE student (
                         id BIGINT NOT NULL AUTO_INCREMENT,
                         number VARCHAR(50) NOT NULL,
                         name VARCHAR(255) NOT NULL,
                         email VARCHAR(255) NOT NULL,
                         current_year INT NOT NULL,

                         CONSTRAINT pk_student PRIMARY KEY (id),
                         CONSTRAINT uk_student_number UNIQUE (number),
                         CONSTRAINT uk_student_email UNIQUE (email),
                         CONSTRAINT chk_student_current_year CHECK (current_year BETWEEN 1 AND 5)
);