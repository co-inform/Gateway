package eu.coinform.gateway.db;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends CrudRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByUser(User user);
}
