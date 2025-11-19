package academy.log_analyzer.util;

import academy.log_analyzer.entity.LogEntry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Instant;

public class ParseUtilTest {

    @Test
    @DisplayName("Парсим nginx лог строку")
    void parseNginxLogString() {
        String logString = "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"";
        LogEntry logEntry = new LogEntry("93.180.71.3", "-", Instant.ofEpochSecond(1431849932), "GET", "/downloads/product_1", "HTTP/1.1", 304, 0, "-", "Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)");
        ParseUtil parseUtil = new ParseUtil();
        LogEntry parsedLogEntry = parseUtil.parseNginxLog(logString);

        Assertions.assertEquals(logEntry, parsedLogEntry);
    }

}
