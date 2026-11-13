package mx.gob.gld.pagos.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TarifaLicenciaRepository extends JpaRepository<TarifaLicenciaEntity, UUID> {

    List<TarifaLicenciaEntity> findByTenantIdOrderByGiroAsc(String tenantId);

    Optional<TarifaLicenciaEntity> findByTenantIdAndGiro(String tenantId, String giro);
}
