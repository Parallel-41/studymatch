package pt.upt.studymatch.grouping.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupingContextTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-23T10:00:00Z"), ZoneOffset.UTC);

    private static GroupingContext context() {
        return new GroupingContext("Software project teams", "Semester project", null, CLOCK);
    }

    @Test
    void createsContextWithCreationMomentAndNoParticipants() {
        GroupingContext context = context();

        assertEquals("Software project teams", context.getName());
        assertEquals("Semester project", context.getDescription());
        assertNull(context.getCourseUnit());
        assertEquals(Instant.parse("2026-11-23T10:00:00Z"), context.getCreatedAt());
        assertTrue(context.getStudentIds().isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsMissingName(String name) {
        assertThrows(IllegalArgumentException.class, () -> new GroupingContext(name, null, null, CLOCK));
    }

    @Test
    void acceptsNameAtMaximumLengthAndRejectsOneMore() {
        String max = "a".repeat(GroupingContext.NAME_MAX_LENGTH);

        assertEquals(max, new GroupingContext(max, null, null, CLOCK).getName());
        assertThrows(IllegalArgumentException.class, () -> new GroupingContext(max + "a", null, null, CLOCK));
    }

    @Test
    void acceptsDescriptionAtMaximumLengthAndRejectsOneMore() {
        String max = "d".repeat(GroupingContext.DESCRIPTION_MAX_LENGTH);

        assertEquals(max, new GroupingContext("Context", max, null, CLOCK).getDescription());
        assertThrows(IllegalArgumentException.class, () -> new GroupingContext("Context", max + "d", null, CLOCK));
    }

    @Test
    void replacesParticipantsAndReturnsThemSorted() {
        GroupingContext context = context();
        context.replaceParticipants(List.of(7L, 2L, 5L));

        context.replaceParticipants(List.of(9L, 3L));

        assertEquals(List.of(3L, 9L), context.getStudentIds());
    }

    @Test
    void acceptsAnEmptyParticipantList() {
        GroupingContext context = context();
        context.replaceParticipants(List.of(1L, 2L));

        context.replaceParticipants(List.of());

        assertTrue(context.getStudentIds().isEmpty());
    }

    @Test
    void rejectsTheSameStudentTwiceAndKeepsPreviousParticipants() {
        GroupingContext context = context();
        context.replaceParticipants(List.of(1L, 2L));

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> context.replaceParticipants(List.of(4L, 5L, 4L))
        );

        assertTrue(error.getMessage().contains("4"));
        assertEquals(List.of(1L, 2L), context.getStudentIds());
    }

    @Test
    void rejectsNullListOrNullId() {
        GroupingContext context = context();

        assertThrows(IllegalArgumentException.class, () -> context.replaceParticipants(null));
        assertThrows(IllegalArgumentException.class, () -> context.replaceParticipants(Arrays.asList(1L, null)));
    }
}
