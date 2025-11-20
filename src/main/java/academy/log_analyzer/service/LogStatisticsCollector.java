package academy.log_analyzer.service;

import academy.log_analyzer.entity.AnalyzedFile;
import academy.log_analyzer.entity.LogEntry;
import academy.log_analyzer.entity.StatisticsReport;
import academy.log_analyzer.util.RoundUtil;
import com.tdunning.math.stats.MergingDigest;
import com.tdunning.math.stats.TDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class LogStatisticsCollector {
    private final Map<Integer, Long> statusCodesStatistics;
    private final Map<String, Long> requestedPaths;
    private final Map<LocalDate, Long> requestsInDate;
    private final Set<String> uniqueProtocols;
    private final TDigest tDigest;
    private long allRequestsCount;
    private long sumResponseSize;
    private long countNotNullResponseSizeRequests;
    private long maxResponseSizeRequest;

    public LogStatisticsCollector() {
        this.allRequestsCount = 0L;
        this.sumResponseSize = 0L;
        this.countNotNullResponseSizeRequests = 0L;
        this.maxResponseSizeRequest = 0L;
        this.statusCodesStatistics = new HashMap<>();
        this.requestedPaths = new HashMap<>();
        this.requestsInDate = new HashMap<>();
        this.uniqueProtocols = new HashSet<>();
        this.tDigest = new MergingDigest(100);
    }

    public void addLogInStatistics(LogEntry logEntry) {
        allRequestsCount++;

        if (logEntry.responseSize() != 0) {
            sumResponseSize += logEntry.responseSize();
            countNotNullResponseSizeRequests++;

            tDigest.add(logEntry.responseSize());

            if (logEntry.responseSize() > maxResponseSizeRequest) {
                maxResponseSizeRequest = logEntry.responseSize();
            }
        }

        statusCodesStatistics.put(logEntry.statusCode(), statusCodesStatistics.getOrDefault(logEntry.statusCode(), 0L) + 1);

        requestedPaths.put(logEntry.path(), requestedPaths.getOrDefault(logEntry.path(), 0L) + 1);

        LocalDate localDate = logEntry.offsetDateTime().toLocalDate();

        requestsInDate.put(localDate, requestsInDate.getOrDefault(localDate, 0L) + 1);

        uniqueProtocols.add(logEntry.protocol());
    }

    private double getAverageResponseSize() {
        return RoundUtil.roundTo2digitsAfterDot((double) sumResponseSize / countNotNullResponseSizeRequests);
    }

    private double getP95() {
        return RoundUtil.roundTo2digitsAfterDot(tDigest.quantile(0.95));
    }


    public StatisticsReport getReport(List<AnalyzedFile> analyzedFiles, LocalDateTime from, LocalDateTime to) {
        return new StatisticsReport(
            getAnalyzedFilesNames(analyzedFiles),
            allRequestsCount,
            getAverageResponseSize(),
            maxResponseSizeRequest,
            getP95(),
            statusCodesStatistics,
            requestedPaths,
            requestsInDate,
            uniqueProtocols,
            from,
            to
        );
    }

    private List<String> getAnalyzedFilesNames(List<AnalyzedFile> analyzedFiles) {
        List<String> analyzedFilesNames = new ArrayList<>();

        for (AnalyzedFile analyzedFile : analyzedFiles) {
            analyzedFilesNames.add(analyzedFile.fileName());
        }
        return analyzedFilesNames;
    }
}
