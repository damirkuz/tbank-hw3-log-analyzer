package academy.log_analyzer.service;

import academy.log_analyzer.entity.LogEntry;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class TimeRangeService {

    private final OffsetDateTime from;
    private final OffsetDateTime to;
    private final LocalDateTime localDateTimeFrom;
    private final LocalDateTime localDateTimeTo;
    private final TimeRangeMode timeRangeMode;

    public TimeRangeService(LocalDateTime from, LocalDateTime to) {
        this.from = toOffsetDateTime(from);
        this.to = toOffsetDateTime(to);
        this.localDateTimeFrom = from;
        this.localDateTimeTo = to;
        this.timeRangeMode = getTimeRangeMode(from, to);
    }

    private OffsetDateTime toOffsetDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atOffset(ZoneOffset.ofHours(0));
    }

    public boolean isCorrectTimeRangeForLogEntry(LogEntry logEntry) {
        return switch (timeRangeMode) {
            case ALL -> true;
            case ALL_BETWEEN_MINUS_INF_AND_TO -> logEntry.offsetDateTime().isBefore(to);
            case ALL_BETWEEN_FROM_AND_INF -> logEntry.offsetDateTime().isAfter(from);
            case ALL_BETWEEN_FROM_AND_TO ->
                logEntry.offsetDateTime().isAfter(from)
                        && logEntry.offsetDateTime().isBefore(to);
        };
    }

    private TimeRangeMode getTimeRangeMode(LocalDateTime from, LocalDateTime to) {
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

    public LocalDateTime getLocalDateTimeFrom() {
        return localDateTimeFrom;
    }

    public LocalDateTime getLocalDateTimeTo() {
        return localDateTimeTo;
    }
}
