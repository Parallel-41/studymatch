package pt.upt.studymatch.academic.infrastructure;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pt.upt.studymatch.common.domain.Grade;

/** Maps the shared Grade value object to the database's integer grade column. */
@Converter
public class GradeAttributeConverter implements AttributeConverter<Grade, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Grade grade) {
        return grade == null ? null : grade.value();
    }

    @Override
    public Grade convertToEntityAttribute(Integer value) {
        return value == null ? null : new Grade(value);
    }
}
