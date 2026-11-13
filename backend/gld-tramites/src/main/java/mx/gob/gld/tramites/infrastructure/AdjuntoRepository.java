package mx.gob.gld.tramites.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdjuntoRepository extends JpaRepository<AdjuntoEntity, UUID> {
}
