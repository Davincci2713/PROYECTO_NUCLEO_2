package mx.gob.gld.ciudad.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventoRepository extends JpaRepository<EventoEntity, UUID> {

    List<EventoEntity> findByTenantIdOrderByIniciaEnAsc(String tenantId);

    Optional<EventoEntity> findByTenantIdAndId(String tenantId, UUID id);
}
