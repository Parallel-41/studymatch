-- Sprint 3 (#101): grouping contexts and their eligible students (ADR-0004).

CREATE TABLE grouping_context (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500) NULL,
    course_unit_id BIGINT NULL,
    created_at DATETIME(6) NOT NULL,

    CONSTRAINT pk_grouping_context PRIMARY KEY (id),
    CONSTRAINT fk_grouping_context_course_unit
        FOREIGN KEY (course_unit_id) REFERENCES course_unit (id)
);

-- The composite primary key stops a student from being added twice to the same context.
CREATE TABLE grouping_participant (
    context_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,

    CONSTRAINT pk_grouping_participant PRIMARY KEY (context_id, student_id),
    CONSTRAINT fk_grouping_participant_context
        FOREIGN KEY (context_id) REFERENCES grouping_context (id) ON DELETE CASCADE,
    CONSTRAINT fk_grouping_participant_student
        FOREIGN KEY (student_id) REFERENCES student (id)
);
