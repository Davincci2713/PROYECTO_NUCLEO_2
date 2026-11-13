package mx.gob.gld.ciudad.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EncuestaRepository extends JpaRepository<EncuestaEntity, UUID> {

    List<EncuestaEntity> findByTenantIdOrderByCierraEnAsc(String tenantId);

    Optional<EncuestaEntity> findByTenantIdAndId(String tenantId, UUID id);
}
