package mu.server.rest.advice;

import lombok.Builder;
import mu.server.service.exception.UsernameExistException;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(exception = {DataIntegrityViolationException.class})
    public ResponseEntity<ErrorMessage> mapUserNotFoundException(DataIntegrityViolationException ex) {
        var errorMessage = ErrorMessage.builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .description(ex.getCause().getMessage())
                .build();

        return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorMessage> mapInvalidCallException(NoResourceFoundException ex) {
        var errorMessage = ErrorMessage.builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .description(ex.getDetailMessageCode())
                .build();

        return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UsernameExistException.class)
    public ResponseEntity<ErrorMessage> mapUsernameExistException(UsernameExistException ex) {
        var errorMessage = ErrorMessage.builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .description(ex.getCause().getMessage())
                .build();

        return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, @Nullable String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, fieldError -> {
                    if (fieldError.getDefaultMessage() != null) {
                        return fieldError.getDefaultMessage();
                    }
                    return "No Default Message";
                }));

        ValidationError validationError = ValidationError.builder()
                .statutCode(HttpStatus.BAD_REQUEST.value())
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .message(ex.getCause().getMessage())
                .build();

        return new ResponseEntity<>(validationError, HttpStatus.BAD_REQUEST);
    }

    @Builder
    public record ErrorMessage(int statusCode, LocalDateTime timestamp, String message, String description) { }

    @Builder
    public record ValidationError(int statutCode, LocalDateTime timestamp, String message, Map<String, String> errors) {}
}
