package mx.gob.gld.incidencias.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BrigadaRepository extends JpaRepository<BrigadaEntity, UUID> {

    Optional<BrigadaEntity> findByTenantIdAndTipo(String tenantId, String tipo);
}
