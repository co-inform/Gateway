package eu.coinform.gateway.db;

import eu.coinform.gateway.db.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    boolean enabled(User user);
    Optional<User> findById(Long userid);
    boolean existsByUuid(String uuid);
}
