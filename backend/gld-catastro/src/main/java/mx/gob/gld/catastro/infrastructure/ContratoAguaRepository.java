package mx.gob.gld.catastro.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContratoAguaRepository extends JpaRepository<ContratoAguaEntity, UUID> {

    Optional<ContratoAguaEntity> findByTenantIdAndNumeroContrato(String tenantId, String numeroContrato);

    List<ContratoAguaEntity> findByTenantIdOrderByNumeroContratoAsc(String tenantId);
}
