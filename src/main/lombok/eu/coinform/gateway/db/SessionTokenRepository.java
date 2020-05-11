package eu.coinform.gateway.db;

import eu.coinform.gateway.db.entity.SessionToken;
import eu.coinform.gateway.db.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface SessionTokenRepository extends CrudRepository<SessionToken, Long> {

    public Optional<List<SessionToken>> findByUser(User user);
    public Optional<SessionToken> findBySessionToken(String token);
//    public Boolean existBySessionToken(String sessionToken);

}
