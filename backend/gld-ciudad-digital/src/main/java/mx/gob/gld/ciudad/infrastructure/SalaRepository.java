package mx.gob.gld.ciudad.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalaRepository extends JpaRepository<SalaEntity, UUID> {

    List<SalaEntity> findByTenantIdOrderByNombreAsc(String tenantId);

    Optional<SalaEntity> findByTenantIdAndId(String tenantId, UUID id);
}
