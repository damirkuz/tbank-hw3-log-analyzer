package academy;

import academy.log_analyzer.exception.DirectoryNotWritableException;
import academy.log_analyzer.exception.InvalidFileFormatException;
import academy.log_analyzer.exception.handler.CommandLineExceptionHandler;
import academy.log_analyzer.exception.handler.ExitCodeMapper;
import academy.log_analyzer.service.LogAnalyzerService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "log-analyzer",
        version = "1.0",
        description = "Log-analyzer CLI application.",
        mixinStandardHelpOptions = true)
public class Application implements Runnable {

    private static final String UNDEFINED_PARAMETER = "undefined";

    public static void main(String[] args) {
        // Логирование входных параметров для проверки работоспособности black-box тестов
        debugArgs(Arrays.asList(args));

        // Запуск программы
        CommandLine cmd = new CommandLine(new Application());
        cmd.setParameterExceptionHandler(new CommandLineExceptionHandler());
        cmd.setExitCodeExceptionMapper(new ExitCodeMapper());
        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }

    // Note: нужно только для отладки, удалить в случае ненадобности
    @Deprecated(forRemoval = true)
    private static void debugArgs(List<String> args) {
        var argsPerParam = getArgumentsPerParameter(args);
        System.out.printf("Входные параметры программы: %s%n", argsPerParam);

        logPaths("Пути к лог-файлам", argsPerParam, "p", "path");
        logPaths("Пути к отчетам", argsPerParam, "o", "output");
    }

    private static Map<String, List<String>> getArgumentsPerParameter(List<String> args) {
        var argsPerParameter = new HashMap<String, List<String>>();
        argsPerParameter.put(UNDEFINED_PARAMETER, new ArrayList<>());

        var queue = new ArrayDeque<>(args);
        String currentParameter = null;
        while (!queue.isEmpty()) {
            var element = queue.removeFirst();
            if (element.startsWith("-")) {
                currentParameter = element.startsWith("--") ? element.substring(2) : element.substring(1);
                argsPerParameter.putIfAbsent(currentParameter, new ArrayList<>());
            } else {
                argsPerParameter
                        .get(Optional.ofNullable(currentParameter).orElse(UNDEFINED_PARAMETER))
                        .add(element);
            }
        }

        return argsPerParameter;
    }

    private static void logPaths(String description, Map<String, List<String>> argsPerParam, String... params) {
        var paths = new ArrayList<String>();
        for (var param : params) {
            paths.addAll(argsPerParam.getOrDefault(param, List.of()));
        }
        System.out.printf(
                "%s: %s%n",
                description,
                paths.stream()
                        .map(it -> it.contains("*")
                                ? "glob: " + it
                                : "path: %s, exists: %s".formatted(it, Files.exists(Path.of(it))))
                        .collect(Collectors.joining(";")));
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
    Date from;

    @Option(
            names = {"--to"},
            description = "конечная точка времени в формате ISO8601")
    Date to;

    @Override
    public void run() {
        LogAnalyzerService service = new LogAnalyzerService();
        try {
            service.analyze(paths, format, output, from, to);
        } catch (InvalidFileFormatException | DirectoryNotWritableException | IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
