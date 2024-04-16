package mu.server.service.exception;

public class InvalidCallException extends RuntimeException {
    public InvalidCallException(String message) {
        super(message);
    }

    public InvalidCallException(String message, Throwable cause) {
        super(message, cause);
    }
}
