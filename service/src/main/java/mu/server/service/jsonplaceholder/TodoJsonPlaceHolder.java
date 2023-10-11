package mu.server.service.jsonplaceholder;

import lombok.Builder;
import mu.server.service.exception.JsonPlaceHolderException;

@Builder
public record TodoJsonPlaceHolder(Long userId, Long id, String title, Boolean completed) {

    public TodoJsonPlaceHolder {
        if (userId == null) throw new JsonPlaceHolderException("userId is null");
    }
}

