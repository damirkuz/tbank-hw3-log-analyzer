package academy.acceptance;

import academy.log_analyzer.service.LogAnalyzerService;
import com.tdunning.math.stats.TDigest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.stream.DoubleStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StatsCalculationTest {

    private LogAnalyzerService analyzerService;
    private Path tempLogFile;
    private Path tempOutputFile;

    @BeforeEach
    void setUp() throws IOException {
        analyzerService = new LogAnalyzerService();
        tempLogFile = Files.createTempFile("test_log", ".log");
        tempOutputFile = Files.createTempFile("test_output", ".json");
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
    @DisplayName("Расчет статистики на основании локального log-файла")
    void happyPathTest() throws Exception {
        String logContent = """
            93.180.71.3 - - [17/May/2015:08:05:32 +0000] "GET /downloads/product_1 HTTP/1.1" 304 100 "-" "Debian APT-HTTP/1.3"
            93.180.71.3 - - [17/May/2015:08:05:33 +0000] "GET /downloads/product_2 HTTP/1.1" 200 200 "-" "Debian APT-HTTP/1.3"
            93.180.71.3 - - [17/May/2015:08:05:34 +0000] "GET /downloads/product_1 HTTP/1.1" 200 300 "-" "Debian APT-HTTP/1.3"
            93.180.71.3 - - [17/May/2015:08:05:35 +0000] "GET /downloads/product_3 HTTP/1.1" 404 400 "-" "Debian APT-HTTP/1.3"
            93.180.71.3 - - [17/May/2015:08:05:36 +0000] "GET /downloads/product_1 HTTP/1.1" 200 500 "-" "Debian APT-HTTP/1.3"
            """;
        Files.writeString(tempLogFile, logContent);

        analyzerService.runAnalysis(
            List.of(tempLogFile.toString()),
            "json",
            tempOutputFile.toString(),
            null,
            null
        );

        assertTrue(Files.exists(tempOutputFile));
        String content = Files.readString(tempOutputFile);

        assertTrue(content.contains("\"totalRequestsCount\""));
        assertTrue(content.contains("\"responseSizeInBytes\""));
        assertTrue(content.contains("\"average\""));
        assertTrue(content.contains("\"max\""));
        assertTrue(content.contains("\"p95\""));
        assertTrue(content.contains("\"resources\""));
        assertTrue(content.contains("\"responseCodes\""));
    }

    @Test
    @DisplayName("TDigest должен соответствовать 95-му процентилю для нормального распределения")
    void testP95OnNormalDistribution() {
        TDigest digest = TDigest.createDigest(100);

        int sampleSize = 1_000_000;
        Random random = new Random();

        DoubleStream.generate(random::nextGaussian)
            .limit(sampleSize)
            .forEach(digest::add);

        double actualP95 = digest.quantile(0.95);
        double expectedP95 = 1.644853626951;
        double tolerance = 0.01;

        assertEquals(expectedP95, actualP95, tolerance);
    }
}
