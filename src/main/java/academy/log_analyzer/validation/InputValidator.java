package academy.log_analyzer.validation;

import academy.log_analyzer.exception.DirectoryNotWritableException;
import academy.log_analyzer.exception.InvalidFileFormatException;
import academy.log_analyzer.format.FormatType;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class InputValidator {

    public void validatePathSuffix(String path) throws InvalidFileFormatException {
        if (!path.endsWith(".txt") && !path.endsWith(".log")) {
            throw new InvalidFileFormatException("Некорректный суффикс файла");
        }
    }

    public void validateOutputFlag(String outputFlag, FormatType formatType)
            throws InvalidFileFormatException, FileAlreadyExistsException, DirectoryNotWritableException {
        if (!isSameOutputFlagAndFileFormat(outputFlag, formatType)) {
            throw new InvalidFileFormatException(
                    "Расширение файла output " + outputFlag + " не соответствует ожидаемому");
        }

        Path path = Path.of(outputFlag);
        if (Files.exists(path)) {
            throw new FileAlreadyExistsException("output файл уже существует");
        }

        Path parent = path.getParent();
        if (parent == null) {
            parent = Path.of(".");
        }

        if (!Files.exists(parent) || !Files.isDirectory(parent)) {
            throw new DirectoryNotWritableException(
                    "Директория " + parent + " не существует или не является директорией");
        }

        if (!Files.isWritable(parent)) {
            throw new DirectoryNotWritableException("Директория " + parent + " недоступна для записи");
        }
    }

    private boolean isSameOutputFlagAndFileFormat(String outputFlag, FormatType formatType) {
        return switch (formatType) {
            case ADOC -> outputFlag.endsWith(".ad");
            case JSON -> outputFlag.endsWith(".json");
            case MARKDOWN -> outputFlag.endsWith(".md");
        };
    }

    public boolean isCorrectUri(String value) {
        try {
            URI uri = new URI(value);
            return uri.getScheme() != null
                    && (uri.getScheme().equalsIgnoreCase("http")
                            || uri.getScheme().equalsIgnoreCase("https"));
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public void validateRemoteUrl(String value) throws IOException {
        if (!isCorrectUri(value)) {
            throw new IllegalArgumentException("Некорректная ссылка: " + value);
        }

        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(value))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());

            int status = response.statusCode();
            if (status == 404) {
                throw new FileNotFoundException("Файл по ссылке не найден: " + value);
            }
            if (status >= 400) {
                throw new IOException("Не удалось получить " + value + ": HTTP " + status);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void validateFromAndTo(LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("Дата флага from больше to");
        }
    }
}
