package mx.gob.gld.pagos.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MultaRepository extends JpaRepository<MultaEntity, UUID> {

    Optional<MultaEntity> findByTenantIdAndFolio(String tenantId, String folio);

    List<MultaEntity> findByTenantIdAndPlacaOrderByFechaInfraccionDesc(String tenantId, String placa);
}
