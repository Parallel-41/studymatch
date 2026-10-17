package pt.upt.studymatch.common.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GradeTest {

    @Test
    void acceptsMinimumGrade() {
        assertEquals(0, new Grade(0).value());
    }

    @Test
    void acceptsMaximumGrade() {
        assertEquals(20, new Grade(20).value());
    }

    @Test
    void considersTenAsPassing() {
        assertTrue(new Grade(10).isPassing());
    }

    @Test
    void considersGradesBelowTenAsFailing() {
        assertFalse(new Grade(9).isPassing());
    }

    @Test
    void rejectsGradeBelowZero() {
        assertThrows(IllegalArgumentException.class, () -> new Grade(-1));
    }

    @Test
    void rejectsGradeAboveTwenty() {
        assertThrows(IllegalArgumentException.class, () -> new Grade(21));
    }
}
