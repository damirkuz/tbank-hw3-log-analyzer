package academy.log_analyzer.format;

import academy.log_analyzer.util.RoundUtil;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public abstract class AbstractFormatter implements Formatter {

    protected final DateTimeFormatter formatterToYYYYMMDD =
        DateTimeFormatter.ofPattern("yyyy-MM-dd");
    protected final DateTimeFormatter formatterToDDMMYYYY =
        DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.ENGLISH);

    protected double getTotalRequestsPercentage(long allRequestsCount, long requestsWithPercent) {
        return RoundUtil.roundTo2digitsAfterDot(((double) requestsWithPercent / allRequestsCount) * 100);
    }

    protected String getStatusName(int code) {
        return switch (code) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 301 -> "Moved Permanently";
            case 302 -> "Found";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            default -> "-";
        };
    }

    protected String formatNumber(long value) {
        String s = Long.toString(value);
        if (s.length() <= 3) {
            return s;
        }
        StringBuilder sb = new StringBuilder();
        int len = s.length();
        int firstGroup = len % 3 == 0 ? 3 : len % 3;

        sb.append(s, 0, firstGroup);
        for (int i = firstGroup; i < len; i += 3) {
            sb.append('_').append(s, i, i + 3);
        }
        return sb.toString();
    }
}
