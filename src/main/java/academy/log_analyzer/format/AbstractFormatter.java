package academy.log_analyzer.format;

import academy.log_analyzer.entity.StatisticsReport;
import academy.log_analyzer.util.RoundUtil;
import java.time.format.DateTimeFormatter;

public abstract class AbstractFormatter implements Formatter {

    protected final DateTimeFormatter formatterToSimpleDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    protected double getTotalRequestsPercentage(long allRequestsCount, long requestsWithPercent) {
        return RoundUtil.roundTo2digitsAfterDot(((double) requestsWithPercent / allRequestsCount) * 100);
    }
}
