package pt.upt.studymatch.profile.domain;

import java.util.Objects;
import pt.upt.studymatch.common.domain.Confidence;
import pt.upt.studymatch.common.domain.Level;

/** Computed level of one competency area. level == null means "no data", never 0. */
public record AreaLevel(String areaCode, Level level, Confidence confidence) {

    public AreaLevel {
        Objects.requireNonNull(areaCode, "areaCode");
        Objects.requireNonNull(confidence, "confidence");
    }

    public static AreaLevel noData(String areaCode) {
        return new AreaLevel(areaCode, null, Confidence.NONE);
    }

    public boolean hasData() {
        return level != null;
    }
}
