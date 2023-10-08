package mu.server.persistence.repository;

import mu.server.persistence.entity.Post;
import mu.server.persistence.repository.custom.GenericRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends GenericRepository<Post> {
}
