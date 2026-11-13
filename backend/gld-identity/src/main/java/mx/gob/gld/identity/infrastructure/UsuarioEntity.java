package mx.gob.gld.identity.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "identity", name = "usuarios")
public class UsuarioEntity {

    @Id
    @Column(name = "id")
    public UUID id;

    @Column(name = "tenant_id", nullable = false, length = 16)
    public String tenantId;

    @Column(name = "nombre", nullable = false, length = 160)
    public String nombre;

    @Column(name = "correo", nullable = false, length = 160)
    public String correo;

    @Column(name = "password_hash", nullable = false, length = 100)
    public String passwordHash;

    @Column(name = "telefono", length = 20)
    public String telefono;

    @Column(name = "rol", nullable = false, length = 32)
    public String rol;

    @Column(name = "correo_verificado", nullable = false)
    public boolean correoVerificado;

    @Column(name = "codigo_verificacion", length = 8)
    public String codigoVerificacion;

    @Column(name = "totp_secreto", length = 64)
    public String totpSecreto;

    @Column(name = "totp_activo", nullable = false)
    public boolean totpActivo;

    @Column(name = "creado_en", nullable = false)
    public Instant creadoEn;
}
