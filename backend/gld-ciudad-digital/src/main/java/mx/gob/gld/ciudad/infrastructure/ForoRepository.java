package mx.gob.gld.ciudad.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ForoRepository extends JpaRepository<ForoEntity, UUID> {

    List<ForoEntity> findByTenantIdOrderByCreadoEnDesc(String tenantId);

    Optional<ForoEntity> findByTenantIdAndId(String tenantId, UUID id);
}
