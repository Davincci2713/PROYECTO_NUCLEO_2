-- Base gld_identity. Sin esquema platform y sin referencia a otra base.

CREATE SCHEMA IF NOT EXISTS identity;

CREATE TABLE identity.usuarios (
    id                   uuid PRIMARY KEY,
    tenant_id            varchar(16)  NOT NULL,
    nombre               varchar(160) NOT NULL,
    correo               varchar(160) NOT NULL,
    password_hash        varchar(100) NOT NULL,
    telefono             varchar(20),
    rol                  varchar(32)  NOT NULL,
    correo_verificado    boolean      NOT NULL DEFAULT false,
    codigo_verificacion  varchar(8),
    totp_secreto         varchar(64),
    totp_activo          boolean      NOT NULL DEFAULT false,
    creado_en            timestamptz  NOT NULL DEFAULT now(),
    UNIQUE (tenant_id, correo),
    CONSTRAINT usuarios_rol_chk CHECK (rol IN ('CIUDADANO', 'SERVIDOR_PUBLICO', 'TESORERO', 'ADMINISTRADOR'))
);
