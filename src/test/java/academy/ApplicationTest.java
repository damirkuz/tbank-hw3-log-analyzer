package academy;

import academy.log_analyzer.service.LogAnalyzerService;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;

@ExtendWith(MockitoExtension.class)
class ApplicationTest {

    @Mock
    LogAnalyzerService service;

    @Test
    @DisplayName("Корректные аргументы, сервис вызывается")
    void happyPathTest() throws Exception {
        Application application = new Application(service);
        CommandLine cmd = new CommandLine(application);

        String[] args = {
            "--path", "nginx_logs.log",
            "--format", "markdown",
            "--output", "report.md"
        };

        int exitCode = cmd.execute(args);

        Assertions.assertEquals(0, exitCode);
        Mockito.verify(service).runAnalysis(List.of("nginx_logs.log"), "markdown", "report.md", null, null);
    }
}
