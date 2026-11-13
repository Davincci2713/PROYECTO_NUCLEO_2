package mx.gob.gld.ciudad.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PerfilRepository extends JpaRepository<PerfilEntity, UUID> {
}
