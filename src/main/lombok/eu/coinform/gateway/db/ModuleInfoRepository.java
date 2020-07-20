package eu.coinform.gateway.db;

import eu.coinform.gateway.db.entity.ModuleInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ModuleInfoRepository extends CrudRepository<ModuleInfo, Long> {
    public Optional<ModuleInfo> getByModulename(String name);
}
