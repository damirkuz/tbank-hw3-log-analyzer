package academy.log_analyzer.validation;

import academy.log_analyzer.exception.DirectoryNotWritableException;
import academy.log_analyzer.exception.InvalidFileFormatException;
import academy.log_analyzer.format.FormatType;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class InputValidator {
    public void validatePathSuffix(String path) throws InvalidFileFormatException {
        if (!path.endsWith(".txt") && !path.endsWith(".log")) {
            throw new InvalidFileFormatException("Некорректный суффикс файла");
        }
    }

    public void validateOutputFlag(String outputFlag, FormatType formatType) throws InvalidFileFormatException, FileAlreadyExistsException, DirectoryNotWritableException {
        if (!isSameOutputFlagAndFileFormat(outputFlag, formatType)) {
            throw new InvalidFileFormatException("Расширение файла output " + outputFlag + " не соответствует ожидаемому");
        }

        // локальные файлы
        Path path = Paths.get(outputFlag);
        if (Files.exists(path)) {
            throw new FileAlreadyExistsException("output файл уже существует");
        }

        Path parent = path.getParent();
        if (parent == null) {
            // файл в текущей директории
            parent = Paths.get(".");
        }

        if (!Files.exists(parent) || !Files.isDirectory(parent)) {
            throw new DirectoryNotWritableException("Директория " + parent + " не существует или не является директорией");
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
}
