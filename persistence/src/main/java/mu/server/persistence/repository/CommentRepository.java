package mu.server.persistence.repository;

import org.springframework.stereotype.Repository;

import mu.server.persistence.entity.Comment;

@Repository
public interface CommentRepository extends GenericRepository<Comment> {
}
