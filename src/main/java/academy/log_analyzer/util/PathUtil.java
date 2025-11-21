package academy.log_analyzer.util;

import academy.log_analyzer.entity.AnalyzedFile;
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
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathUtil {

    private static final Logger log = LoggerFactory.getLogger(PathUtil.class);
    private final InputValidator inputValidator = new InputValidator();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public List<AnalyzedFile> getAllBufferedReadersFromPaths(List<String> paths)
        throws IOException, InvalidFileFormatException {
        List<AnalyzedFile> result = new ArrayList<>();

        for (String path : paths) {
            if (inputValidator.isCorrectUri(path)) {
                log.info("Загрузка удаленного файла: {}", path);
                inputValidator.validateRemoteUrl(path);
                try {
                    result.add(readFileFromUrl(path));
                } catch (InterruptedException e) {
                    throw new FileNotFoundException("Ошибка при получении файла по пути " + path);
                }
            } else {
                Path p = Path.of(path);
                if (Files.exists(p)) {
                    log.info("Чтение файла: {}", path);
                    inputValidator.validatePathSuffix(path);
                    result.add(new AnalyzedFile(p.getFileName().toString(), Files.newBufferedReader(p)));
                } else {
                    log.info("Поиск файлов по шаблону: {}", path);
                    List<AnalyzedFile> expanded = expandLocalPattern(path);
                    if (expanded.isEmpty()) {
                        throw new FileNotFoundException("Локальные файл(ы) по пути " + path + " не найден(ы)");
                    }
                    result.addAll(expanded);
                }
            }
        }

        return result;
    }

    private AnalyzedFile readFileFromUrl(String uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        return new AnalyzedFile(uri, new BufferedReader(new InputStreamReader(response.body())));
    }

    private List<AnalyzedFile> expandLocalPattern(String pattern) throws IOException {
        Path p = Path.of(pattern);

        Path dir;
        String fileGlob;

        Path parent = p.getParent();
        if (parent == null) {
            dir = Path.of(".");
            fileGlob = pattern;
        } else {
            dir = parent;
            Path fileName = p.getFileName();
            if (fileName == null) {
                return new ArrayList<>();
            }
            fileGlob = fileName.toString();
        }

        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            return new ArrayList<>();
        }

        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + fileGlob);

        List<AnalyzedFile> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                Path entryFileName = entry.getFileName();
                if (entryFileName != null && matcher.matches(entryFileName)) {
                    result.add(new AnalyzedFile(entryFileName.toString(), Files.newBufferedReader(entry)));
                }
            }
        }

        return result;
    }
}
