CREATE TABLE self_assessment (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 student_id BIGINT NOT NULL,
                                 area_code VARCHAR(10) NOT NULL,
                                 level DOUBLE NOT NULL CHECK (level >= 0 AND level <= 100),
                                 assessed_at DATE NOT NULL,
                                 UNIQUE KEY uk_student_area (student_id, area_code),
                                 FOREIGN KEY (student_id) REFERENCES student(id)
);
