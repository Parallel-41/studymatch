package pt.upt.studymatch.profile.application;

public class CompetencyNotFoundException extends RuntimeException {

    public CompetencyNotFoundException(String competencyCode) {
        super("Competency " + competencyCode + " not found in the student's degree programme");
    }
}
