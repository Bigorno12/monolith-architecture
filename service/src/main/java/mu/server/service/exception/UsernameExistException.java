package mu.server.service.exception;

public class UsernameExistException extends RuntimeException {
    public UsernameExistException(String message) {
        super(message);
    }
}
