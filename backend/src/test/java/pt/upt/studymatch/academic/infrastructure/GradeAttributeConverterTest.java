package pt.upt.studymatch.academic.infrastructure;

import org.junit.jupiter.api.Test;
import pt.upt.studymatch.common.domain.Grade;

import static org.junit.jupiter.api.Assertions.*;

class GradeAttributeConverterTest {

    private final GradeAttributeConverter converter = new GradeAttributeConverter();

    @Test
    void convertsGradeToDatabaseInteger() {
        assertEquals(17, converter.convertToDatabaseColumn(new Grade(17)));
    }

    @Test
    void convertsDatabaseIntegerToGrade() {
        assertEquals(new Grade(15), converter.convertToEntityAttribute(15));
    }

    @Test
    void convertsNullGradeToNullDatabaseValue() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertsNullDatabaseValueToNullGrade() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void rejectsInvalidDatabaseGrade() {
        assertThrows(
            IllegalArgumentException.class,
            () -> converter.convertToEntityAttribute(21)
        );
    }
}
