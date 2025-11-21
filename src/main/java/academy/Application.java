package academy;

import academy.log_analyzer.exception.DirectoryNotWritableException;
import academy.log_analyzer.exception.InvalidFileFormatException;
import academy.log_analyzer.exception.handler.CommandLineExceptionHandler;
import academy.log_analyzer.exception.handler.ExitCodeMapper;
import academy.log_analyzer.service.LogAnalyzerService;
import java.io.IOException;
import java.util.List;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "log-analyzer",
        version = "1.0",
        description = "Log-analyzer CLI application.",
        mixinStandardHelpOptions = true)
public class Application implements Runnable {

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new Application());
        cmd.setParameterExceptionHandler(new CommandLineExceptionHandler());
        cmd.setExitCodeExceptionMapper(new ExitCodeMapper());
        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }

    @Option(
            names = {"--path", "-p"},
            required = true,
            arity = "1..*",
            description = "путь к одному или нескольким NGINX лог-файлам")
    List<String> paths;

    @Option(
            names = {"--format", "-f"},
            required = true,
            description = "формат вывода результатов: json, markdown, adoc")
    String format;

    @Option(
            names = {"--output", "-o"},
            required = true,
            description = "путь до файла, куда должен быть сохранён результат работы программы")
    String output;

    @Option(
            names = {"--from"},
            description = "стартовая точка времени в формате ISO8601")
    String from;

    @Option(
            names = {"--to"},
            description = "конечная точка времени в формате ISO8601")
    String to;

    private final LogAnalyzerService service;

    public Application() {
        this(new LogAnalyzerService());
    }

    Application(LogAnalyzerService service) {
        this.service = service;
    }

    @Override
    public void run() {
        try {
            service.runAnalysis(paths, format, output, from, to);
        } catch (InvalidFileFormatException | DirectoryNotWritableException | IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
