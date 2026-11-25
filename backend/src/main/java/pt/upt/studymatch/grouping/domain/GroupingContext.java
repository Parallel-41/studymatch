package pt.upt.studymatch.grouping.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import pt.upt.studymatch.academic.domain.CourseUnit;

import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** An academic activity (project, assignment...) for which groups are generated, with its eligible students. */
@Entity
@Table(name = "grouping_context")
public class GroupingContext {

    public static final int NAME_MAX_LENGTH = 120;
    public static final int DESCRIPTION_MAX_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = NAME_MAX_LENGTH)
    private String name;

    @Column(length = DESCRIPTION_MAX_LENGTH)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_unit_id")
    private CourseUnit courseUnit;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ElementCollection
    @CollectionTable(name = "grouping_participant", joinColumns = @JoinColumn(name = "context_id"))
    @Column(name = "student_id", nullable = false)
    private Set<Long> studentIds = new HashSet<>();

    protected GroupingContext() {
        // Required by JPA.
    }

    public GroupingContext(String name, String description, CourseUnit courseUnit, Clock clock) {
        validateName(name);
        validateDescription(description);
        this.name = name;
        this.description = description;
        this.courseUnit = courseUnit;
        this.createdAt = Instant.now(clock);
    }

    /** Replaces the eligible students; the same student cannot appear twice (brief §3.1). */
    public void replaceParticipants(List<Long> newStudentIds) {
        if (newStudentIds == null) {
            throw new IllegalArgumentException("studentIds is required.");
        }
        Set<Long> unique = new HashSet<>();
        for (Long studentId : newStudentIds) {
            if (studentId == null) {
                throw new IllegalArgumentException("studentIds must not contain null.");
            }
            if (!unique.add(studentId)) {
                throw new IllegalArgumentException("Student " + studentId + " appears more than once.");
            }
        }
        studentIds.clear();
        studentIds.addAll(unique);
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required.");
        }
        if (name.length() > NAME_MAX_LENGTH) {
            throw new IllegalArgumentException("name must not exceed " + NAME_MAX_LENGTH + " characters.");
        }
    }

    private static void validateDescription(String description) {
        if (description != null && description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new IllegalArgumentException(
                "description must not exceed " + DESCRIPTION_MAX_LENGTH + " characters."
            );
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public CourseUnit getCourseUnit() {
        return courseUnit;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Participant ids in ascending order, so API responses are stable. */
    public List<Long> getStudentIds() {
        return studentIds.stream().sorted().toList();
    }
}
