package eu.coinform.gateway.db;

import eu.coinform.gateway.db.entity.Role;
import org.springframework.data.repository.CrudRepository;

public interface RoleRepository extends CrudRepository<Role, Long> {
}
