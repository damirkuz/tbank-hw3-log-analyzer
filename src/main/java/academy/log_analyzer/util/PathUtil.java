package academy.log_analyzer.util;

import academy.log_analyzer.exception.InvalidFileFormatException;
import academy.log_analyzer.validation.InputValidator;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PathUtil {

    private final InputValidator inputValidator = new InputValidator();


    public List<BufferedReader> getAllBufferedReadersFromPaths(List<String> paths) throws IOException, InvalidFileFormatException {
        List<BufferedReader> result = new ArrayList<>();

        for (String path : paths) {
            if (inputValidator.isCorrectUri(path)) {
                // веб файл
                inputValidator.validateRemoteUrl(path);
                try {
                    result.add(readFileFromUrl(path));
                } catch (InterruptedException e) {
                    throw new FileNotFoundException("Ошибка при получении файла по пути " + path);
                }

            } else {
                // локальные файлы
                Path p = Paths.get(path);
                if (Files.exists(p)) {
                    // обычный файл
                    inputValidator.validatePathSuffix(path);
                    result.add(Files.newBufferedReader(p));
                } else {
                    // glob шаблон
                    List<BufferedReader> expanded = expandLocalPattern(path);
                    if (expanded.isEmpty()) {
                        throw new FileNotFoundException("Локальные файл(ы) по пути " + path + " не найден(ы)");
                    }
                    result.addAll(expanded);
                }
            }
        }


        return result;
    }

    private BufferedReader readFileFromUrl(String uri) throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .build();

            HttpResponse<InputStream> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofInputStream()
            );

            return new BufferedReader(new InputStreamReader(response.body()));
        }
    }

    private List<BufferedReader> expandLocalPattern(String pattern) throws IOException {
        Path p = Paths.get(pattern);

        Path dir;
        String fileGlob;

        if (p.getParent() == null) {
            dir = Paths.get("."); // текущая директория
            fileGlob = pattern;
        } else {
            dir = p.getParent();
            fileGlob = p.getFileName().toString();
        }

        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + fileGlob);

        List<BufferedReader> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (matcher.matches(entry.getFileName())) {
                    result.add(Files.newBufferedReader(entry));
                }
            }
        }

        return result;
    }
}
