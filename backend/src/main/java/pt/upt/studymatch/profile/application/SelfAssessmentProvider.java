package pt.upt.studymatch.profile.application;

import java.util.Map;
import pt.upt.studymatch.common.domain.Level;

/**
 * Source of a student's self-assessment, one level per competency area (ADR-0003, A6).
 * Implemented by the self-assessment feature (#52); while no implementation exists,
 * the profile is computed from academic evidence only.
 */
public interface SelfAssessmentProvider {

    /** Area code to self-assessed level; an empty map when the student has not answered. */
    Map<String, Level> levelsByArea(long studentId);
}
