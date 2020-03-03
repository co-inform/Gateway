package eu.coinform.gateway.db;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {

//    User findByVerificationToken(VerificationToken verificationToken);
    boolean enabled(User user);
}
