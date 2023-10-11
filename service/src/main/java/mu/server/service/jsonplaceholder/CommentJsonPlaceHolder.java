package mu.server.service.jsonplaceholder;

import lombok.Builder;
import mu.server.service.exception.JsonPlaceHolderException;

@Builder
public record CommentJsonPlaceHolder(Long postId, Long id, String name, String email, String body) {

    public CommentJsonPlaceHolder {
        if (postId == null) throw new JsonPlaceHolderException("postId is null");
    }
}
