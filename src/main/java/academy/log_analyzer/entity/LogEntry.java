package academy.log_analyzer.entity;

import java.time.Instant;

public record LogEntry (
    String userIP,
    String remoteUser,
    Instant timestamp,
    String method,
    String path,
    String protocol,
    int statusCode,
    long responseSize,
    String referer,
    String userAgent
) {}
