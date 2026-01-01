package mu.server.service.dto;

import lombok.Builder;

@Builder
public record TodosRequest(Long userId, Long id, String title, Boolean completed) {
}
