package mu.server.persistence.repository;

import mu.server.persistence.entity.User;
import mu.server.persistence.projections.NamesOnly;
import mu.server.persistence.repository.blaze.impl.UserCustomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends GenericRepository<User>, UserCustomRepository {

    Page<User> findByFirstnameContainsIgnoreCase(Pageable pageable, String firstname);

    // Projections
    NamesOnly findNamesOnlyById(Long id);

    Page<NamesOnly> findNamesOnlyByFirstnameContainsIgnoreCase(Pageable pageable, String firstname);

    // To remove boilerplate code
    <T> T findById(Long id, Class<T> clazz);
}
