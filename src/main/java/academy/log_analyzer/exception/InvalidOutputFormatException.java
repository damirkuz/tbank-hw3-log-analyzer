package academy.log_analyzer.exception;

public class InvalidOutputFormatException extends Exception {
    public InvalidOutputFormatException() {
        super();
    }

    public InvalidOutputFormatException(String message) {
        super(message);
    }
}
