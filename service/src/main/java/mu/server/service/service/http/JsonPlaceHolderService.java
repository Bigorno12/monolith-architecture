package mu.server.service.service.http;

import mu.server.service.dto.TodosRequest;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange("/todos")
public interface JsonPlaceHolderService {

    @GetExchange(version = "1.0", accept = "application/json")
    List<TodosRequest> todos();

    @GetExchange(version = "1.0", accept = "application/json")
    TodosRequest todo(@RequestParam(name = "userId", defaultValue = "1") Long userId);
}
