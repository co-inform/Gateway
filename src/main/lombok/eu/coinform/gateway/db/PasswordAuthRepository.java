package eu.coinform.gateway.db;

import org.springframework.data.repository.CrudRepository;

public interface PasswordAuthRepository extends CrudRepository<PasswordAuth, User> {
    public PasswordAuth getByEmail(String email);
    public Boolean existsByEmail(String email);
}
