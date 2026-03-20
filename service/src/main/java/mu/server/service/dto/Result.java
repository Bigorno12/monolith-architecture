package mu.server.service.dto;

import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
public record Result<T>(@Nullable T value, @Nullable String error, boolean success) {

    public static <T> Result<T> ok(T value) {
        return new Result<>(value, null, true);
    }

    public static <T> Result<T> failure(String error) {
        return new Result<>(null, error, false);
    }
}
