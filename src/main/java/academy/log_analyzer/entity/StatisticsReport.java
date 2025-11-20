package academy.log_analyzer.entity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record StatisticsReport(
    List<String> analyzedFiles,
    long allRequestsCount,
    double averageResponseSize,
    long maxResponseSizeRequest,
    double percentile95,
    Map<Integer, Long> statusCodesStatistics,
    Map<String, Long> requestedPaths,
    Map<LocalDate, Long> requestsInDate,
    Set<String> uniqueProtocols
) {}
