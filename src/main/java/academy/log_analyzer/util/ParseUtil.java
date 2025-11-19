package academy.log_analyzer.util;

import academy.log_analyzer.entity.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseUtil {

    // Примеры логов
    // '$remote_addr - $remote_user [$time_local] ' '"$request" $status $body_bytes_sent ' '"$http_referer" "$http_user_agent"'
    //93.180.71.3 - - [17/May/2015:08:05:32 +0000] "GET /downloads/product_1 HTTP/1.1" 304 0 "-" "Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)"

    private static final Logger log = LoggerFactory.getLogger(ParseUtil.class);

    private static final Pattern splitIntoParts = Pattern.compile("^((\\d{1,3}\\.){3}\\d{1,3}) - (.+?) \\[(.+?)\\] \\\"(.+?)\\\"$");
    private static final Pattern splitRequest = Pattern.compile("([A-Za-z]+?) (.+?) (.+?)\\\" (\\d+) (\\d+) \\\"(.+)\\\" \\\"(.+)");

    private static final DateTimeFormatter NGINX_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);


    public LogEntry parseNginxLog(String string) {
        Matcher matcherOnGroups = splitIntoParts.matcher(string);
        if (!matcherOnGroups.matches()) {
            log.warn("Строка {} не соответствует стандартному формату", string);
            return null;
        }
        Matcher matcherRequest = splitRequest.matcher(matcherOnGroups.group(5));
        if (!matcherRequest.matches()) {
            log.warn("Строка {} не соответствует стандартному формату", string);
            return null;
        }

        String userIP = matcherOnGroups.group(1);

        Instant timestamp = parseTimestamp(matcherOnGroups.group(4));

        String remoteUser = matcherOnGroups.group(3);

        // "GET /downloads/product_1 HTTP/1.1" 304 0 "-" "Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)"
        String method = matcherRequest.group(1);
        String path = matcherRequest.group(2);
        String protocol = matcherRequest.group(3);
        int status = Integer.parseInt(matcherRequest.group(4));
        int responseSize = Integer.parseInt(matcherRequest.group(5));
        String referer = matcherRequest.group(6);
        String userAgent = matcherRequest.group(7);

        return new LogEntry(userIP, remoteUser, timestamp, method, path, protocol, status, responseSize, referer, userAgent);
    }


    private Instant parseTimestamp(String timestampString) {
        // 	17/May/2015:08:05:32 +0000
        OffsetDateTime odt = OffsetDateTime.parse(timestampString, NGINX_DATE_FORMATTER);
        return odt.toInstant();
    }

}
