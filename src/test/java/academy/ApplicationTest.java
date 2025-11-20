package academy;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import java.io.File;

public class ApplicationTest {

    @Test
    @DisplayName("Базовая проверка работоспособности программы")
    void happyPathTest() {
        Application application = new Application();

        String[] args = {"--path", "nginx_logs.log", "--format", "markdown", "--output", "report2.md"};
        CommandLine cmd = new CommandLine(application);

        System.out.println(cmd.execute(args));
    }
}
