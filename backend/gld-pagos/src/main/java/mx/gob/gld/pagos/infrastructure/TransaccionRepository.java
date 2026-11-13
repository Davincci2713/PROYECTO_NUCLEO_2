package mx.gob.gld.pagos.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransaccionRepository extends JpaRepository<TransaccionEntity, UUID> {
}
