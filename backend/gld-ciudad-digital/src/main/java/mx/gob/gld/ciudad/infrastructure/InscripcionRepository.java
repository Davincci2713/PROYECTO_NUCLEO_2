package mx.gob.gld.ciudad.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InscripcionRepository extends JpaRepository<InscripcionEntity, UUID> {

    List<InscripcionEntity> findByUsuarioId(UUID usuarioId);

    Optional<InscripcionEntity> findByEventoIdAndUsuarioId(UUID eventoId, UUID usuarioId);
}
