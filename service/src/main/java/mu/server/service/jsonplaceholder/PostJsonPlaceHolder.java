package mu.server.service.jsonplaceholder;

import lombok.Builder;
import mu.server.service.exception.JsonPlaceHolderException;

@Builder
public record PostJsonPlaceHolder(Long userId, Long id, String title, String body) {

    public PostJsonPlaceHolder {
        if (userId == null) throw new JsonPlaceHolderException("userId is null");
    }
}

