package mx.gob.gld.pagos.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LiquidacionRepository extends JpaRepository<LiquidacionEntity, UUID> {

    Optional<LiquidacionEntity> findByTenantIdAndId(String tenantId, UUID id);

    Optional<LiquidacionEntity> findByTenantIdAndTipoAndReferenciaAndEstado(
            String tenantId, String tipo, String referencia, String estado);
}
