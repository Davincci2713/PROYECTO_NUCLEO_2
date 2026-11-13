package mx.gob.gld.ciudad.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OpcionEncuestaRepository extends JpaRepository<OpcionEncuestaEntity, UUID> {

    List<OpcionEncuestaEntity> findByEncuestaIdInOrderByIdAsc(Collection<UUID> encuestaIds);

    List<OpcionEncuestaEntity> findByEncuestaIdOrderByIdAsc(UUID encuestaId);
}
