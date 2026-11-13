package mx.gob.gld.catastro.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PredioRepository extends JpaRepository<PredioEntity, UUID> {

    Optional<PredioEntity> findByTenantIdAndClaveCatastral(String tenantId, String claveCatastral);
}
