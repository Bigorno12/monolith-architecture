package mu.server.rest.advice;

import java.time.LocalDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import mu.server.service.exception.JsonPlaceHolderException;
import mu.server.service.exception.KeycloakException;
import mu.server.service.exception.NotFoundException;
import mu.server.service.exception.RegistrationRejectedException;
import mu.server.service.exception.UsernameExistException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorMessage> mapDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error("Data integrity violation", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "The request conflicts with existing data");
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ErrorMessage> mapInvalidDataAccessApiUsageException(InvalidDataAccessApiUsageException ex) {
        log.error("Invalid data access", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "The request could not be processed");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorMessage> mapNoResourceFoundException(NoResourceFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({UsernameExistException.class, RegistrationRejectedException.class, JsonPlaceHolderException.class})
    public ResponseEntity<ErrorMessage> mapBadRequest(RuntimeException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler({KeycloakException.class, NotFoundException.class})
    public ResponseEntity<ErrorMessage> mapNotFound(RuntimeException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    private static ResponseEntity<ErrorMessage> error(HttpStatus status, String message) {
        var errorMessage = ErrorMessage.builder()
                .statusCode(status.value())
                .timestamp(LocalDateTime.now())
                .message(message)
                .build();

        return new ResponseEntity<>(errorMessage, status);
    }

    @Builder
    public record ErrorMessage(int statusCode, LocalDateTime timestamp, String message) {
    }
}
