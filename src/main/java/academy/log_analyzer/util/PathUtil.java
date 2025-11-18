package academy.log_analyzer.util;

import academy.log_analyzer.exception.InvalidFileFormatException;
import academy.log_analyzer.validation.InputValidator;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class PathUtil {
    public List<Path> getAllPaths(String path) throws IOException, InvalidFileFormatException {
        List<Path> result = new ArrayList<>();

        if (isUrl(path)) {
            try {
                result.add(downloadToTempFile(path));
            } catch (URISyntaxException e) {
                throw new FileNotFoundException("Удалённые файл(ы) по пути " + path + " не найден(ы)");
            }
        } else {
            // локальные файлы
            Path p = Paths.get(path);
            if (Files.exists(p)) {
                // обычный файл
                InputValidator.validatePathSuffix(path);
                result.add(p.toAbsolutePath().normalize());

            } else {
                // glob шаблон
                List<Path> expanded = expandLocalPattern(path);
                if (expanded.isEmpty()) {
                    throw new FileNotFoundException("Локальные файл(ы) по пути " + path + " не найден(ы)");
                }
                result.addAll(expanded);
            }
        }

        return result;
    }

    private boolean isUrl(String value) {
        try {
            URI uri = new URI(value);
            return uri.getScheme() != null
                    && (uri.getScheme().equalsIgnoreCase("http")
                            || uri.getScheme().equalsIgnoreCase("https"));
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private Path downloadToTempFile(String urlString) throws IOException, URISyntaxException {
        URI uri = new URI(urlString);
        URL url = uri.toURL();
        Path tempFile = Files.createTempFile("nginx-log-", ".log");

        try (InputStream in = url.openStream()) {
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFile;
    }

    private List<Path> expandLocalPattern(String pattern) throws IOException {
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

        List<Path> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (matcher.matches(entry.getFileName())) {
                    result.add(entry.toAbsolutePath().normalize());
                }
            }
        }
        return result;
    }
}
