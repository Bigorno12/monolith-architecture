package mu.server.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record AuthenticationRequest(@NotNull(message = "Username must not be Null")
                                    @NotBlank(message = "Username must not be blank") String username,
                                    @NotNull(message = "Password must not be Null")
                                    @NotBlank(message = "Password must not be blank")
                                    String password) {
}
