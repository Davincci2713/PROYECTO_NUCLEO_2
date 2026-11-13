package mx.gob.gld.ciudad.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface MensajeForoRepository extends JpaRepository<MensajeForoEntity, UUID> {

    List<MensajeForoEntity> findByForoIdIn(Collection<UUID> foroIds);

    List<MensajeForoEntity> findByForoIdOrderByCreadoEnAsc(UUID foroId);
}
