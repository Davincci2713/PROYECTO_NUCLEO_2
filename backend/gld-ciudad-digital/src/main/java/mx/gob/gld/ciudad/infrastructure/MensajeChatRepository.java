package mx.gob.gld.ciudad.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MensajeChatRepository extends JpaRepository<MensajeChatEntity, UUID> {

    List<MensajeChatEntity> findBySalaIdOrderByCreadoEnAsc(UUID salaId);
}
