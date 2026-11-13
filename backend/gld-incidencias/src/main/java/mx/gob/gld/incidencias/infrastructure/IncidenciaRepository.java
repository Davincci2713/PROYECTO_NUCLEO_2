package mx.gob.gld.incidencias.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncidenciaRepository extends JpaRepository<IncidenciaEntity, UUID> {

    Optional<IncidenciaEntity> findByTenantIdAndFolio(String tenantId, String folio);

    Optional<IncidenciaEntity> findByTenantIdAndId(String tenantId, UUID id);

    List<IncidenciaEntity> findByTenantIdAndCiudadanoIdOrderByCreadaEnDesc(String tenantId, UUID ciudadanoId);
}
