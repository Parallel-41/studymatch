package pt.upt.studymatch.academic.api;

import java.util.List;

public record HistoryResponse(List<Year> years) {
    public record Year(String academicYear, List<Semester> semesters) {}
    public record Semester(int semester, List<Unit> units) {}
    public record Unit(String code, String name, int ects, List<Attempt> attempts, Integer bestGrade) {}
    public record Attempt(String season, Integer grade) {}
}
