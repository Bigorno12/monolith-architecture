package mu.server.rest.advice;

import mu.server.service.exception.ErrorMessage;
import mu.server.service.exception.JsonPlaceHolderException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(JsonPlaceHolderException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorMessage jsonPlaceHolderBadRequestHandler(JsonPlaceHolderException ex, WebRequest webRequest) {
        return ErrorMessage.builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now())
                .message(ex.getMessage())
                .description(webRequest.getDescription(false))
                .build();
    }
}
