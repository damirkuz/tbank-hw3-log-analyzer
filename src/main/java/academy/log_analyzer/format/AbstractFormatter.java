package academy.log_analyzer.format;

import academy.log_analyzer.util.RoundUtil;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public abstract class AbstractFormatter implements Formatter {

    protected final DateTimeFormatter formatterToYYYYMMDD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    protected final DateTimeFormatter formatterToDDMMYYYY =
        DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.ENGLISH);

    protected double getTotalRequestsPercentage(long allRequestsCount, long requestsWithPercent) {
        return RoundUtil.roundTo2digitsAfterDot(((double) requestsWithPercent / allRequestsCount) * 100);
    }
}
