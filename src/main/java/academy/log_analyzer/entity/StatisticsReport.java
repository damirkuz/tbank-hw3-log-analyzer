package academy.log_analyzer.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record StatisticsReport(
    List<String> analyzedFilesNames,
    long allRequestsCount,
    double averageResponseSize,
    long maxResponseSizeRequest,
    double percentile95,
    Map<Integer, Long> statusCodesStatistics,
    Map<String, Long> requestedPaths,
    Map<LocalDate, Long> requestsInDate,
    Set<String> uniqueProtocols,
    LocalDateTime from,
    LocalDateTime to
) {}
