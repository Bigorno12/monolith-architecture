package mu.server.persistence.repository;

import mu.server.persistence.entity.Comment;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends GenericRepository<Comment> {
}
