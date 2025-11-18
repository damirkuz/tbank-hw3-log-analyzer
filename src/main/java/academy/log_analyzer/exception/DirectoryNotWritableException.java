package academy.log_analyzer.exception;

public class DirectoryNotWritableException extends Exception {
    public DirectoryNotWritableException() {
        super();
    }

    public DirectoryNotWritableException(String message) {
        super(message);
    }
}
