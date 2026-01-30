package mu.server.service.service.http;

import mu.server.service.dto.todo.TodoRequest;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange("/todos")
public interface JsonPlaceHolderService {

    @GetExchange(version = "1.0", accept = "application/json")
    @Retryable(maxRetries = 4L, delay = 2000, multiplier = 2.0, maxDelay = 4000L)
    List<TodoRequest> todo(@RequestParam(name = "userId", defaultValue = "1") Long userId);
}
