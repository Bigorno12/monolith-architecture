package mu.server.service.dto.todo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record TodoRequest(@NotBlank(message = "Title must not be null or blank") String title,
                          @NotNull(message = "Completed must not be null") Boolean completed) {
}
