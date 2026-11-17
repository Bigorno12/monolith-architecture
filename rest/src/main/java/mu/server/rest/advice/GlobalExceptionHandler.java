package mu.server.rest.advice;

import lombok.Builder;
import mu.server.service.exception.InvalidCredentialsException;
import mu.server.service.exception.JsonPlaceHolderException;
import mu.server.service.exception.KeycloakException;
import mu.server.service.exception.NotFoundException;
import mu.server.service.exception.UsernameExistException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorMessage> mapDataIntegrityViolationException(DataIntegrityViolationException ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorMessage> mapNoResourceFoundException(NoResourceFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), ex.getDetailMessageCode());
    }

    @ExceptionHandler(UsernameExistException.class)
    public ResponseEntity<ErrorMessage> mapUsernameExistException(UsernameExistException ex) {
        return error(HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorMessage> mapInvalidCredentialsException(InvalidCredentialsException ex) {
        return error(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
    }

    @ExceptionHandler(KeycloakException.class)
    public ResponseEntity<ErrorMessage> mapKeycloakException(KeycloakException ex) {
        return error(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorMessage> mapNotFoundException(NotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(JsonPlaceHolderException.class)
    public ResponseEntity<ErrorMessage> mapJsonPlaceHolderException(JsonPlaceHolderException ex) {
        return error(HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ErrorMessage> mapInvalidDataAccessApiUsageException(InvalidDataAccessApiUsageException ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }

    private static ResponseEntity<ErrorMessage> error(HttpStatus status, Exception ex) {
        return error(status, ex.getMessage(), ex.getCause() != null ? ex.getCause().getMessage() : null);
    }

    private static ResponseEntity<ErrorMessage> error(HttpStatus status, String message, String description) {
        var errorMessage = ErrorMessage.builder()
                .statusCode(status.value())
                .timestamp(LocalDateTime.now())
                .message(message)
                .description(description)
                .build();

        return new ResponseEntity<>(errorMessage, status);
    }

    @Builder
    public record ErrorMessage(int statusCode, LocalDateTime timestamp, String message, String description) {
    }
}
