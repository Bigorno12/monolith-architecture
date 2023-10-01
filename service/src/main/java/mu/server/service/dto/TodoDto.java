package mu.server.service.dto;

import lombok.Builder;

@Builder
public record TodoDto(Long id,  UserDto userDto, String title, Boolean completed) { }
