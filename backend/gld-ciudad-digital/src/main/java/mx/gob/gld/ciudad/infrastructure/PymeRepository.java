package mx.gob.gld.ciudad.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PymeRepository extends JpaRepository<PymeEntity, UUID> {

    @Query("""
            select p from PymeEntity p
            where p.tenantId = :tenant
              and (:q = '' or lower(p.nombre) like lower(concat('%', :q, '%'))
                         or lower(p.giro) like lower(concat('%', :q, '%'))
                         or lower(p.colonia) like lower(concat('%', :q, '%')))
            order by p.nombre asc
            """)
    List<PymeEntity> buscar(@Param("tenant") String tenant, @Param("q") String q);
}
