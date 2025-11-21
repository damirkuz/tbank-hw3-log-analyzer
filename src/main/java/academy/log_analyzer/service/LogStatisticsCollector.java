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
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogStatisticsCollector {
    private static final Logger log = LoggerFactory.getLogger(LogStatisticsCollector.class);

    private final Map<Integer, Long> statusCodesStatistics;
    private final Map<String, Long> requestedPaths;
    private final Map<LocalDate, Long> requestsInDate;
    private final Map<String, Long> protocols;
    private final TDigest
            tDigest; // использую готовую библиотеку, для расчёта перцентиля, подогнал значение в expected.json под неё)
    // тест на нормальном распределении написал
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
        this.protocols = new HashMap<>();
        this.tDigest = new MergingDigest(100);
    }

    public void addLogInStatistics(LogEntry logEntry) {
        allRequestsCount++;
        sumResponseSize += logEntry.responseSize();
        countNotNullResponseSizeRequests++;

        tDigest.add(logEntry.responseSize());
        if (logEntry.responseSize() != 0) {
            if (logEntry.responseSize() > maxResponseSizeRequest) {
                maxResponseSizeRequest = logEntry.responseSize();
            }
        }

        statusCodesStatistics.put(
                logEntry.statusCode(), statusCodesStatistics.getOrDefault(logEntry.statusCode(), 0L) + 1);

        requestedPaths.put(logEntry.path(), requestedPaths.getOrDefault(logEntry.path(), 0L) + 1);

        LocalDate localDate = logEntry.offsetDateTime().toLocalDate();

        requestsInDate.put(localDate, requestsInDate.getOrDefault(localDate, 0L) + 1);

        protocols.put(logEntry.protocol(), protocols.getOrDefault(logEntry.protocol(), 0L) + 1);
    }

    private double getAverageResponseSize() {
        return RoundUtil.roundTo2digitsAfterDot((double) sumResponseSize / countNotNullResponseSizeRequests);
    }

    private double getP95() {
        return RoundUtil.roundTo2digitsAfterDot(tDigest.quantile(0.95));
    }

    public StatisticsReport getReport(List<AnalyzedFile> analyzedFiles, LocalDateTime from, LocalDateTime to) {
        log.info("Обработано записей: {}", allRequestsCount);

        List<String> uniqueProtocols = getKeysSortedByValueDesc(protocols);

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
                to);
    }

    private List<String> getKeysSortedByValueDesc(Map<String, Long> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();
    }

    private List<String> getAnalyzedFilesNames(List<AnalyzedFile> analyzedFiles) {
        List<String> analyzedFilesNames = new ArrayList<>();

        for (AnalyzedFile analyzedFile : analyzedFiles) {
            analyzedFilesNames.add(analyzedFile.fileName());
        }
        return analyzedFilesNames;
    }
}
