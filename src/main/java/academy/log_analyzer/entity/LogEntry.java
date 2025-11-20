package academy.log_analyzer.entity;

import java.time.OffsetDateTime;

public record LogEntry(
        String userIP,
        String remoteUser,
        OffsetDateTime offsetDateTime,
        String method,
        String path,
        String protocol,
        int statusCode,
        long responseSize,
        String referer,
        String userAgent) {}
