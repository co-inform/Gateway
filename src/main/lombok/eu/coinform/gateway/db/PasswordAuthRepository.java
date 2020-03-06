package eu.coinform.gateway.db;

import eu.coinform.gateway.db.entity.PasswordAuth;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PasswordAuthRepository extends CrudRepository<PasswordAuth, Long> {
    public Optional<PasswordAuth> getByEmail(String email);
    public Boolean existsByEmail(String email);
}
