package mu.server.persistence.repository;

import mu.server.persistence.entity.User;
import mu.server.persistence.projections.NamesOnly;
import mu.server.persistence.repository.blaze.impl.UserCustomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends GenericRepository<User>, UserCustomRepository {

    Page<User> findByNameContainsIgnoreCase(Pageable pageable, String name);

    // Projections
    NamesOnly findNamesOnlyById(Long id);

    Page<NamesOnly> findNamesOnlyByNameContainsIgnoreCase(Pageable pageable, String name);

    // To remove boilerplate code
    <T> T findById(Long id, Class<T> clazz);
}
