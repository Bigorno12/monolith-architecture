package mu.server.service.exception;

public class JsonPlaceHolderException extends RuntimeException {

    public JsonPlaceHolderException(String message) {
        super(message);
    }

    public JsonPlaceHolderException(String message, Throwable cause) {
        super(message, cause);
    }
}
