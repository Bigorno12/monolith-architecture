package mu.server.persistence.repository;

import mu.server.persistence.entity.Post;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends GenericRepository<Post> {

    @Modifying
    @Query("""
        DELETE FROM Post p WHERE p.user.id = :userId
    """)
    void deleteByUserId(@Param("userId") Long userId);
}
