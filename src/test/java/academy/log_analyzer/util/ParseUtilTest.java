package academy.log_analyzer.util;

import academy.log_analyzer.entity.LogEntry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class ParseUtilTest {

        @Test
        @DisplayName("Парсим nginx лог строку")
        void parseNginxLogString() {
            String logString = "54.77.119.190 - - [23/May/2015:22:05:13 +0000] \"GET /downloads/product_2 HTTP/1.1\" 404 336 \"-\" \"Debian APT-HTTP/1.3 (1.0.1ubuntu2)\"";
            LogEntry logEntry = new LogEntry("54.77.119.190", "-",
                OffsetDateTime.of(LocalDateTime.parse("2015-05-23T22:05:13"),ZoneOffset.ofHours(0)), "GET",
     "/downloads/product_2", "HTTP/1.1", 404, 336, "-", "Debian APT-HTTP/1.3 (1.0.1ubuntu2)");
            ParseUtil parseUtil = new ParseUtil();
            LogEntry parsedLogEntry = parseUtil.parseNginxLog(logString);
            Assertions.assertEquals(logEntry, parsedLogEntry);
        }

}
