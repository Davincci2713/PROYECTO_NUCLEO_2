package mx.gob.gld.identity.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, UUID> {

    @Query("select u from UsuarioEntity u where u.tenantId = :tenant and lower(u.correo) = lower(:correo)")
    Optional<UsuarioEntity> buscar(@Param("tenant") String tenant, @Param("correo") String correo);
}
