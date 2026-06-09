package mu.server.persistence.repository

import mu.server.persistence.entity.Todo
import mu.server.persistence.repository.blaze.impl.TodoCustomRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TodoRepository : GenericRepository<Todo>, TodoCustomRepository {

    @Modifying
    @Query("""
        DELETE FROM Todo t WHERE t.user.id = :userId
    """)
    fun deleteByUserId(@Param("userId") userId: String)
}