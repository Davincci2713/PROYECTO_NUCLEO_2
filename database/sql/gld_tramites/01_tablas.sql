-- Base gld_tramites. liquidacion_id no referencia otra base.

CREATE SCHEMA IF NOT EXISTS tramites;

CREATE SEQUENCE tramites.folio_seq START 1;

CREATE TABLE tramites.tramites (
    id              uuid PRIMARY KEY,
    tenant_id       varchar(16)  NOT NULL,
    folio           varchar(32)  NOT NULL,
    tipo            varchar(40)  NOT NULL,
    estado          varchar(32)  NOT NULL,
    ciudadano_id    uuid         NOT NULL,
    titulo          varchar(200) NOT NULL,
    payload         jsonb        NOT NULL DEFAULT '{}'::jsonb,
    liquidacion_id  uuid,
    creado_en       timestamptz  NOT NULL DEFAULT now(),
    actualizado_en  timestamptz  NOT NULL DEFAULT now(),
    UNIQUE (tenant_id, folio),
    CONSTRAINT tramite_tipo_chk CHECK (tipo IN ('LICENCIA_FUNCIONAMIENTO', 'LICENCIA_CONSTRUCCION')),
    CONSTRAINT tramite_estado_chk CHECK (estado IN (
        'RADICADO', 'EN_REVISION', 'POR_SUBSANAR', 'APROBADO_PENDIENTE_PAGO', 'PAGADO', 'APROBADO', 'CERRADO'
    ))
);

CREATE TABLE tramites.historial (
    id          uuid PRIMARY KEY,
    tramite_id  uuid         NOT NULL REFERENCES tramites.tramites (id),
    estado      varchar(32)  NOT NULL,
    actor       varchar(160) NOT NULL,
    nota        varchar(500) NOT NULL,
    creado_en   timestamptz  NOT NULL DEFAULT now()
);

CREATE TABLE tramites.adjuntos (
    id            uuid PRIMARY KEY,
    tramite_id    uuid         NOT NULL REFERENCES tramites.tramites (id),
    nombre        varchar(180) NOT NULL,
    content_type  varchar(80)  NOT NULL,
    contenido     bytea        NOT NULL,
    creado_en     timestamptz  NOT NULL DEFAULT now()
);
