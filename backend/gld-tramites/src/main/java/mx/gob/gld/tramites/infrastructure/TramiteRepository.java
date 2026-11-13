package mx.gob.gld.tramites.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TramiteRepository extends JpaRepository<TramiteEntity, UUID> {

    Optional<TramiteEntity> findByTenantIdAndFolio(String tenantId, String folio);

    List<TramiteEntity> findByTenantIdAndCiudadanoIdOrderByCreadoEnDesc(String tenantId, UUID ciudadanoId);

    List<TramiteEntity> findByTenantIdAndEstadoInOrderByCreadoEnAsc(String tenantId, Collection<String> estados);
}
