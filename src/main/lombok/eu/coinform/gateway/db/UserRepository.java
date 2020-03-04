package eu.coinform.gateway.db;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

//    User findByVerificationToken(VerificationToken verificationToken);
    boolean enabled(User user);
    Optional<User> findById(Long userid);
}
