package mx.gob.gld.tramites.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HistorialRepository extends JpaRepository<HistorialEntity, UUID> {

    List<HistorialEntity> findByTramiteIdOrderByCreadoEnAsc(UUID tramiteId);
}
