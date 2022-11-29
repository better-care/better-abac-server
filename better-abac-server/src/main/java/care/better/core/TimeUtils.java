package care.better.core;

import java.time.OffsetDateTime;

/**
 * @author Matic Ribic
 */
public final class TimeUtils {

    private TimeUtils() {
    }

    public static boolean equalOffsetDateTime(OffsetDateTime offsetDateTime, OffsetDateTime other) {
        return offsetDateTime != null && other != null ? offsetDateTime.isEqual(other) : (offsetDateTime == null && other == null);
    }
}
