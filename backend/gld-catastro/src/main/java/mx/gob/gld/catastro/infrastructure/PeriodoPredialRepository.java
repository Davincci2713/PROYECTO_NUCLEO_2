package mx.gob.gld.catastro.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PeriodoPredialRepository extends JpaRepository<PeriodoPredialEntity, UUID> {

    List<PeriodoPredialEntity> findByPredioIdOrderByFechaVencimientoAsc(UUID predioId);
}
