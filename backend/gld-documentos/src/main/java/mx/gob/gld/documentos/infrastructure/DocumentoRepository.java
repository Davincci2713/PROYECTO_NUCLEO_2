package mx.gob.gld.documentos.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DocumentoRepository extends JpaRepository<DocumentoEntity, UUID> {

    Optional<DocumentoEntity> findByTenantIdAndId(String tenantId, UUID id);

    Optional<DocumentoEntity> findByTenantIdAndFolio(String tenantId, String folio);
}
