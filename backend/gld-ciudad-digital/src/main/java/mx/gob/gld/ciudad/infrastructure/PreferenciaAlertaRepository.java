package mx.gob.gld.ciudad.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PreferenciaAlertaRepository extends JpaRepository<PreferenciaAlertaEntity, UUID> {

    List<PreferenciaAlertaEntity> findByTenantId(String tenantId);
}
