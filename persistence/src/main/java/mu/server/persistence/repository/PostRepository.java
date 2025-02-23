package mu.server.persistence.repository;

import mu.server.persistence.entity.Post;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends GenericRepository<Post> {
}
