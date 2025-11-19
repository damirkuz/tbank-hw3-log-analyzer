package academy.log_analyzer.util;

import academy.log_analyzer.entity.LogEntry;
import java.util.Date;

public class TimeRangeUtil {
    public static boolean isCorrectTimeRangeForLogEntry(LogEntry logEntry, Date from, Date to, TimeRangeMode timeRangeMode) {
        return switch (timeRangeMode) {
            case ALL -> true;
            case ALL_BETWEEN_MINUS_INF_AND_TO -> logEntry.timestamp().isBefore(to.toInstant());
            case ALL_BETWEEN_FROM_AND_INF -> logEntry.timestamp().isAfter(from.toInstant());
            case ALL_BETWEEN_FROM_AND_TO -> logEntry.timestamp().isAfter(from.toInstant()) && logEntry.timestamp().isBefore(to.toInstant());
        };
    }

    public static TimeRangeMode getTimeRangeMode(Date from, Date to) {
        if (from == null && to == null) {
            return TimeRangeMode.ALL;
        } else if (from == null) {
            return TimeRangeMode.ALL_BETWEEN_MINUS_INF_AND_TO;
        } else if (to == null) {
            return TimeRangeMode.ALL_BETWEEN_FROM_AND_INF;
        } else {
            return TimeRangeMode.ALL_BETWEEN_FROM_AND_TO;
        }
    }
}
