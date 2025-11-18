package academy.log_analyzer.exception;

public class InvalidFileFormatException extends Exception {
    public InvalidFileFormatException() {
        super();
    }

    public InvalidFileFormatException(String message) {
        super(message);
    }
}
