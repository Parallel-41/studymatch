package pt.upt.studymatch.competency.domain;

/** How much one course unit contributes to one competency, weight in ]0, 1]. */
public record CourseUnitWeight(String courseUnitCode, String competencyCode, double weight) {
    public CourseUnitWeight {
        if (courseUnitCode == null || courseUnitCode.isBlank()
            || competencyCode == null || competencyCode.isBlank()) {
            throw new IllegalArgumentException("Course unit and competency codes are required");
        }
        if (!(weight > 0 && weight <= 1)) {
            throw new IllegalArgumentException("Weight must be in ]0, 1], got " + weight);
        }
    }
}
