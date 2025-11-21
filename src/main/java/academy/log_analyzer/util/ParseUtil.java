package academy.log_analyzer.util;

import academy.log_analyzer.entity.LogEntry;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParseUtil {

    private static final Logger log = LoggerFactory.getLogger(ParseUtil.class);

    private static final Pattern SPLIT_INTO_PARTS = Pattern.compile("^(.+?) - (.+?) \\[(.+?)\\] \"(.+?)\"$");
    private static final Pattern SPLIT_REQUEST =
            Pattern.compile("([A-Za-z]+?) (.+?) (.+?)\\\" (\\d+) (\\d+) \\\"(.+)\\\" \\\"(.+)");

    private static final DateTimeFormatter NGINX_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("d/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);

    public LogEntry parseNginxLog(String string) {
        Matcher matcherOnGroups = SPLIT_INTO_PARTS.matcher(string);
        if (!matcherOnGroups.matches()) {
            log.warn("Строка не соответствует стандартному формату");
            return null;
        }
        Matcher matcherRequest = SPLIT_REQUEST.matcher(matcherOnGroups.group(4));
        if (!matcherRequest.matches()) {
            log.warn("Строка не соответствует стандартному формату");
            return null;
        }

        String userIP = matcherOnGroups.group(1);
        OffsetDateTime dateTime = parseDateTime(matcherOnGroups.group(3));
        String remoteUser = matcherOnGroups.group(2);

        String method = matcherRequest.group(1);
        String path = matcherRequest.group(2);
        String protocol = matcherRequest.group(3);
        int status = Integer.parseInt(matcherRequest.group(4));
        int responseSize = Integer.parseInt(matcherRequest.group(5));
        String referer = matcherRequest.group(6);
        String userAgent = matcherRequest.group(7);

        return new LogEntry(
                userIP, remoteUser, dateTime, method, path, protocol, status, responseSize, referer, userAgent);
    }

    private OffsetDateTime parseDateTime(String dateTimeString) {
        return OffsetDateTime.parse(dateTimeString, NGINX_DATE_FORMATTER);
    }

    public LocalDateTime parseLocalDateTime(String text) {
        if (text == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(text);
        } catch (Exception ignored) {
            LocalDate date = LocalDate.parse(text);
            return date.atStartOfDay();
        }
    }
}
