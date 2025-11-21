package academy.acceptance;

import academy.log_analyzer.service.LogAnalyzerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StatsReportTest {

    private LogAnalyzerService analyzerService;
    private Path tempLogFile;
    private Path tempOutputFile;

    @BeforeEach
    void setUp() throws IOException {
        analyzerService = new LogAnalyzerService();
        tempLogFile = Files.createTempFile("test_log", ".log");

        String logContent = """
            93.180.71.3 - - [17/May/2015:08:05:32 +0000] "GET /downloads/product_1 HTTP/1.1" 304 100 "-" "Debian APT-HTTP/1.3"
            93.180.71.3 - - [17/May/2015:08:05:33 +0000] "GET /downloads/product_2 HTTP/1.1" 200 200 "-" "Debian APT-HTTP/1.3"
            93.180.71.3 - - [17/May/2015:08:05:34 +0000] "GET /downloads/product_1 HTTP/1.1" 200 300 "-" "Debian APT-HTTP/1.3"
            """;
        Files.writeString(tempLogFile, logContent);
    }

    @AfterEach
    void cleanup() throws IOException {
        if (tempLogFile != null && Files.exists(tempLogFile)) {
            Files.deleteIfExists(tempLogFile);
        }
        if (tempOutputFile != null && Files.exists(tempOutputFile)) {
            Files.deleteIfExists(tempOutputFile);
        }
    }

    @Test
    @DisplayName("Сохранение статистики в формате JSON")
    void jsonTest() throws Exception {
        tempOutputFile = Files.createTempFile("test_output", ".json");
        Files.deleteIfExists(tempOutputFile);

        analyzerService.runAnalysis(
            List.of(tempLogFile.toString()),
            "json",
            tempOutputFile.toString(),
            null,
            null
        );

        assertTrue(Files.exists(tempOutputFile));
        String content = Files.readString(tempOutputFile);

        assertTrue(content.contains("{"));
        assertTrue(content.contains("}"));
        assertTrue(content.contains("\"totalRequestsCount\""));
        assertTrue(content.contains("\"responseSizeInBytes\""));
    }

    @Test
    @DisplayName("Сохранение статистики в формате MARKDOWN")
    void markdownTest() throws Exception {
        tempOutputFile = Files.createTempFile("test_output", ".md");
        Files.deleteIfExists(tempOutputFile);

        analyzerService.runAnalysis(
            List.of(tempLogFile.toString()),
            "markdown",
            tempOutputFile.toString(),
            null,
            null
        );

        assertTrue(Files.exists(tempOutputFile));
        String content = Files.readString(tempOutputFile);

        assertTrue(content.contains("|"));
        assertTrue(content.contains("Метрика"));
    }

    @Test
    @DisplayName("Сохранение статистики в формате ADOC")
    void adocTest() throws Exception {
        tempOutputFile = Files.createTempFile("test_output", ".ad");
        Files.deleteIfExists(tempOutputFile);

        analyzerService.runAnalysis(
            List.of(tempLogFile.toString()),
            "adoc",
            tempOutputFile.toString(),
            null,
            null
        );

        assertTrue(Files.exists(tempOutputFile));
        assertTrue(Files.size(tempOutputFile) > 0);
    }
}
