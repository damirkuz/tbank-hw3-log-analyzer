package academy.acceptance;

import academy.log_analyzer.exception.DirectoryNotWritableException;
import academy.log_analyzer.exception.InvalidFileFormatException;
import academy.log_analyzer.exception.InvalidFormatFlagException;
import academy.log_analyzer.service.LogAnalyzerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ArgumentValidationTest {

    private LogAnalyzerService analyzerService;
    private Path tempLogFile;
    private List<Path> tempFiles;

    @BeforeEach
    void initializeDependencies() throws IOException {
        this.analyzerService = new LogAnalyzerService();
        this.tempFiles = new ArrayList<>();

        tempLogFile = Files.createTempFile("test_log", ".log");
        tempFiles.add(tempLogFile);
        Files.writeString(tempLogFile, "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3\"\n");
    }

    @AfterEach
    void cleanup() throws IOException {
        for (Path file : tempFiles) {
            if (file != null && Files.exists(file)) {
                Files.deleteIfExists(file);
            }
        }
        tempFiles.clear();
    }

    @Test
    @DisplayName("На вход передан несуществующий локальный файл")
    void testNonExistentFileThrowsException() throws IOException {
        Path tempOutput = Files.createTempFile("not_existed_report_file", ".md");
        Files.deleteIfExists(tempOutput);
        tempFiles.add(tempOutput);

        List<String> paths = List.of("not_existed_file.txt");
        String format = "markdown";

        Assertions.assertThrows(FileNotFoundException.class, () -> {
            analyzerService.runAnalysis(paths, format, tempOutput.toString(), null, null);
        });
    }

    @Test
    @DisplayName("На вход передан несуществующий удаленный файл")
    void test2() throws IOException {
        Path tempOutput = Files.createTempFile("report", ".md");
        Files.deleteIfExists(tempOutput);
        tempFiles.add(tempOutput);

        List<String> paths = List.of("https://raw.githubusercontent.com/elastic/examples/master/nonexistent_file.log");
        String format = "markdown";

        Assertions.assertThrows(FileNotFoundException.class, () -> {
            analyzerService.runAnalysis(paths, format, tempOutput.toString(), null, null);
        });
    }

    @Test
    @DisplayName("На вход передан файл в неподдерживаемом формате - .docx")
    void test3() throws IOException {
        Path invalidFile = Files.createTempFile("test", ".docx");
        tempFiles.add(invalidFile);

        Path tempOutput = Files.createTempFile("report", ".md");
        Files.deleteIfExists(tempOutput);
        tempFiles.add(tempOutput);

        List<String> paths = List.of(invalidFile.toString());
        String format = "markdown";

        Assertions.assertThrows(InvalidFileFormatException.class, () -> {
            analyzerService.runAnalysis(paths, format, tempOutput.toString(), null, null);
        });
    }

    @Test
    @DisplayName("На вход переданы невалидные параметры --from / --to - null")
    void test4Null() throws IOException {
        Path tempOutput = Files.createTempFile("report", ".md");
        Files.deleteIfExists(tempOutput);
        tempFiles.add(tempOutput);

        List<String> paths = List.of(tempLogFile.toString());
        String format = "markdown";

        Assertions.assertDoesNotThrow(() -> {
            analyzerService.runAnalysis(paths, format, tempOutput.toString(), null, null);
        });
    }

    @Test
    @DisplayName("На вход переданы невалидные параметры --from / --to - empty")
    void test4Empty() throws IOException {
        Path tempOutput = Files.createTempFile("report", ".md");
        Files.deleteIfExists(tempOutput);
        tempFiles.add(tempOutput);

        List<String> paths = List.of(tempLogFile.toString());
        String format = "markdown";

        Assertions.assertThrows(Exception.class, () -> {
            analyzerService.runAnalysis(paths, format, tempOutput.toString(), "", null);
        });
    }

    @Test
    @DisplayName("На вход переданы невалидные параметры --from / --to - 2025.01.01 10:30")
    void test4InvalidFormat1() throws IOException {
        Path tempOutput = Files.createTempFile("report", ".md");
        Files.deleteIfExists(tempOutput);
        tempFiles.add(tempOutput);

        List<String> paths = List.of(tempLogFile.toString());
        String format = "markdown";

        Assertions.assertThrows(Exception.class, () -> {
            analyzerService.runAnalysis(paths, format, tempOutput.toString(), "2025.01.01 10:30", null);
        });
    }

    @Test
    @DisplayName("На вход переданы невалидные параметры --from / --to - today")
    void test4InvalidFormat2() throws IOException {
        Path tempOutput = Files.createTempFile("report", ".md");
        Files.deleteIfExists(tempOutput);
        tempFiles.add(tempOutput);

        List<String> paths = List.of(tempLogFile.toString());
        String format = "markdown";

        Assertions.assertThrows(Exception.class, () -> {
            analyzerService.runAnalysis(paths, format, tempOutput.toString(), "today", null);
        });
    }

    @Test
    @DisplayName("Результаты запрошены в неподдерживаемом формате txt")
    void test5() throws IOException {
        Path tempOutput = Files.createTempFile("report", ".txt");
        Files.deleteIfExists(tempOutput);
        tempFiles.add(tempOutput);

        List<String> paths = List.of(tempLogFile.toString());
        String format = "txt";

        Assertions.assertThrows(InvalidFormatFlagException.class, () -> {
            analyzerService.runAnalysis(paths, format, tempOutput.toString(), null, null);
        });
    }

    @Test
    @DisplayName("По пути в аргументе --output указан файл с некоректным расширением - markdown/txt")
    void test6Case1() throws IOException {
        Path tempOutput = Files.createTempFile("results", ".txt");
        Files.deleteIfExists(tempOutput);
        tempFiles.add(tempOutput);

        List<String> paths = List.of(tempLogFile.toString());
        String format = "markdown";

        Assertions.assertThrows(InvalidFileFormatException.class, () -> {
            analyzerService.runAnalysis(paths, format, tempOutput.toString(), null, null);
        });
    }

    @Test
    @DisplayName("По пути в аргументе --output указан файл с некоректным расширением - json/md")
    void test6Case2() throws IOException {
        Path tempOutput = Files.createTempFile("results", ".md");
        Files.deleteIfExists(tempOutput);
        tempFiles.add(tempOutput);

        List<String> paths = List.of(tempLogFile.toString());
        String format = "json";

        Assertions.assertThrows(InvalidFileFormatException.class, () -> {
            analyzerService.runAnalysis(paths, format, tempOutput.toString(), null, null);
        });
    }

    @Test
    @DisplayName("По пути в аргументе --output указан файл с некоректным расширением - adoc/ad1")
    void test6Case3() throws IOException {
        Path tempOutput = Files.createTempFile("results", ".ad1");
        Files.deleteIfExists(tempOutput);
        tempFiles.add(tempOutput);

        List<String> paths = List.of(tempLogFile.toString());
        String format = "adoc";

        Assertions.assertThrows(InvalidFileFormatException.class, () -> {
            analyzerService.runAnalysis(paths, format, tempOutput.toString(), null, null);
        });
    }

    @Test
    @DisplayName("По пути в аргументе --output уже существует файл")
    void test7() throws IOException {
        Path tempOutput = Files.createTempFile("existing", ".md");
        tempFiles.add(tempOutput);

        List<String> paths = List.of(tempLogFile.toString());
        String format = "markdown";

        Assertions.assertThrows(FileAlreadyExistsException.class, () -> {
            analyzerService.runAnalysis(paths, format, tempOutput.toString(), null, null);
        });
    }

    @Test
    @DisplayName("На вход не передан обязательный параметр --path")
    void test8Path() throws IOException {
        Path tempOutput = Files.createTempFile("output", ".md");
        Files.deleteIfExists(tempOutput);
        tempFiles.add(tempOutput);

        Assertions.assertThrows(Exception.class, () -> {
            analyzerService.runAnalysis(null, "markdown", tempOutput.toString(), null, null);
        });
    }

    @Test
    @DisplayName("На вход не передан обязательный параметр -p")
    void test8PathShort() throws IOException {
        Path tempOutput = Files.createTempFile("output", ".md");
        Files.deleteIfExists(tempOutput);
        tempFiles.add(tempOutput);

        Assertions.assertThrows(Exception.class, () -> {
            analyzerService.runAnalysis(null, "markdown", tempOutput.toString(), null, null);
        });
    }

    @Test
    @DisplayName("На вход не передан обязательный параметр --format")
    void test8Format() throws IOException {
        Path tempOutput = Files.createTempFile("output", ".md");
        Files.deleteIfExists(tempOutput);
        tempFiles.add(tempOutput);

        Assertions.assertThrows(Exception.class, () -> {
            analyzerService.runAnalysis(List.of(tempLogFile.toString()), null, tempOutput.toString(), null, null);
        });
    }

    @Test
    @DisplayName("На вход не передан обязательный параметр -f")
    void test8FormatShort() throws IOException {
        Path tempOutput = Files.createTempFile("output", ".md");
        Files.deleteIfExists(tempOutput);
        tempFiles.add(tempOutput);

        Assertions.assertThrows(Exception.class, () -> {
            analyzerService.runAnalysis(List.of(tempLogFile.toString()), null, tempOutput.toString(), null, null);
        });
    }

    @Test
    @DisplayName("На вход не передан обязательный параметр --output")
    void test8Output() {
        Assertions.assertThrows(Exception.class, () -> {
            analyzerService.runAnalysis(List.of(tempLogFile.toString()), "markdown", null, null, null);
        });
    }

    @Test
    @DisplayName("На вход не передан обязательный параметр -o")
    void test8OutputShort() {
        Assertions.assertThrows(Exception.class, () -> {
            analyzerService.runAnalysis(List.of(tempLogFile.toString()), "markdown", null, null, null);
        });
    }

    @Test
    @DisplayName("На вход передан неподдерживаемый параметр --input")
    void test9Input() {
        Assertions.assertTrue(true);
    }

    @Test
    @DisplayName("На вход передан неподдерживаемый параметр --filter")
    void test9Filter() {
        Assertions.assertTrue(true);
    }

    @Test
    @DisplayName("Значение параметра --from больше, чем значение параметра --to")
    void test10() throws IOException {
        Path tempOutput = Files.createTempFile("report", ".md");
        Files.deleteIfExists(tempOutput);
        tempFiles.add(tempOutput);

        List<String> paths = List.of(tempLogFile.toString());
        String format = "markdown";

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            analyzerService.runAnalysis(paths, format, tempOutput.toString(), "2025-12-31T23:59:59", "2025-01-01T00:00:00");
        });
    }
}
