package eu.coinform.gateway.db;

import org.springframework.data.repository.CrudRepository;

public interface VerificationTokenRepository extends CrudRepository<VerificationToken, Long> {

    VerificationToken findByToken(String token);
    VerificationToken findByUser(User user);

}
