package eu.coinform.gateway.db;

import eu.coinform.gateway.db.entity.SessionToken;
import eu.coinform.gateway.db.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    boolean enabled(User user);
    Optional<User> findById(Long userid);
    Optional<User> findBySessionToken(SessionToken sessionToken);
}
