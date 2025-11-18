package academy.log_analyzer.validation;

import academy.log_analyzer.exception.InvalidFileFormatException;

public class InputValidator {
    public static void validatePathSuffix(String path) throws InvalidFileFormatException {
        if (!path.endsWith(".txt") && !path.endsWith(".log")) {
            throw new InvalidFileFormatException("Некорректный суффикс файла");
        }
    }
}
