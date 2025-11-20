package academy.format;

import academy.log_analyzer.entity.LogEntry;
import academy.log_analyzer.format.Formatter;
import academy.log_analyzer.format.JsonFormatter;
import academy.log_analyzer.util.ParseUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Instant;

public class JsonFormatterTest {

    @Test
    @DisplayName("Проверяем какая получается строка")
    void parseNginxLogString() {
        Formatter formatter = new JsonFormatter();

        System.out.println(formatter.format(null));

    }
}
