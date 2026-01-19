package mu.server.service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import mu.server.persistence.enumeration.Gender;

@Builder
public record UpdateUserRequest(@NotNull(message = "Firstname must not be null") String firstname,
                                @NotNull(message = "Lastname must not be null") String lastname,
                                @Min(value = 16, message = "Age should be greater than 16")
                                @Max(value = 100, message = "Age should be less than 100") Integer age,
                                Gender gender,
                                @NotBlank(message = "Email must not be blank") String email,
                                @NotBlank(message = "Username must not be blank") String username) {
}
