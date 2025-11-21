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

public class LogFileParsingTest {

    private LogAnalyzerService analyzerService;
    private Path tempLogFile;
    private Path tempOutputFile;

    @BeforeEach
    void setUp() throws IOException {
        analyzerService = new LogAnalyzerService();
        tempLogFile = Files.createTempFile("test_log", ".log");
        tempOutputFile = Files.createTempFile("test_output", ".md");
        Files.deleteIfExists(tempOutputFile);
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
    @DisplayName("На вход передан валидный локальный log-файл")
    void localFileProcessingTest() throws Exception {
        String logContent = """
            93.180.71.3 - - [17/May/2015:08:05:32 +0000] "GET /downloads/product_1 HTTP/1.1" 304 0 "-" "Debian APT-HTTP/1.3"
            93.180.71.3 - - [17/May/2015:08:05:23 +0000] "GET /downloads/product_2 HTTP/1.1" 200 512 "-" "Debian APT-HTTP/1.3"
            """;
        Files.writeString(tempLogFile, logContent);

        analyzerService.runAnalysis(
            List.of(tempLogFile.toString()),
            "markdown",
            tempOutputFile.toString(),
            null,
            null
        );

        assertTrue(Files.exists(tempOutputFile));
        assertTrue(Files.size(tempOutputFile) > 0);
    }

    @Test
    @DisplayName("На вход передан валидный удаленный log-файл")
    void remoteFileProcessingTest() throws Exception {
        String remoteUrl = "https://raw.githubusercontent.com/elastic/examples/master/Common%20Data%20Formats/nginx_logs/nginx_logs";

        analyzerService.runAnalysis(
            List.of(remoteUrl),
            "markdown",
            tempOutputFile.toString(),
            null,
            null
        );

        assertTrue(Files.exists(tempOutputFile));
        assertTrue(Files.size(tempOutputFile) > 0);
    }

    @Test
    @DisplayName("На вход передан валидный локальный log-файл, часть строк в котором нужно отфильтровать по --from и --to")
    void localFileProcessingAndFilteringTest() throws Exception {
        String logContent = """
            93.180.71.3 - - [17/May/2015:08:05:32 +0000] "GET /downloads/product_1 HTTP/1.1" 304 0 "-" "Debian APT-HTTP/1.3"
            93.180.71.3 - - [18/May/2015:08:05:23 +0000] "GET /downloads/product_2 HTTP/1.1" 200 512 "-" "Debian APT-HTTP/1.3"
            93.180.71.3 - - [19/May/2015:08:05:23 +0000] "GET /downloads/product_3 HTTP/1.1" 200 1024 "-" "Debian APT-HTTP/1.3"
            """;
        Files.writeString(tempLogFile, logContent);

        analyzerService.runAnalysis(
            List.of(tempLogFile.toString()),
            "markdown",
            tempOutputFile.toString(),
            "2015-05-18T00:00:00",
            "2015-05-18T23:59:59"
        );

        assertTrue(Files.exists(tempOutputFile));
        String content = Files.readString(tempOutputFile);
        assertTrue(content.contains("Количество запросов"));
    }

    @Test
    @DisplayName("На вход передан локальный log-файл, часть строк в котором не подходит под формат")
    void damagedLocalFileProcessingTest() throws Exception {
        String logContent = """
            93.180.71.3 - - [17/May/2015:08:05:32 +0000] "GET /downloads/product_1 HTTP/1.1" 304 0 "-" "Debian APT-HTTP/1.3"
            This is an invalid log line
            Another invalid line without proper format
            93.180.71.3 - - [18/May/2015:08:05:23 +0000] "GET /downloads/product_2 HTTP/1.1" 200 512 "-" "Debian APT-HTTP/1.3"
            """;
        Files.writeString(tempLogFile, logContent);

        analyzerService.runAnalysis(
            List.of(tempLogFile.toString()),
            "markdown",
            tempOutputFile.toString(),
            null,
            null
        );

        assertTrue(Files.exists(tempOutputFile));
        assertTrue(Files.size(tempOutputFile) > 0);
    }
}
