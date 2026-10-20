package pt.upt.studymatch.academic.api;

import pt.upt.studymatch.academic.domain.CourseUnit;

public record CourseUnitResponse(String code, String name, int curricularYear, int semester, int ects) {
    public static CourseUnitResponse from(CourseUnit unit) {
        return new CourseUnitResponse(unit.getCode(), unit.getName(), unit.getCurricularYear(), unit.getSemester(), unit.getEcts());
    }
}
