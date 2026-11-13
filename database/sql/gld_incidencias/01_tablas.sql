-- Base gld_incidencias. PostGIS solo en esta base.

CREATE EXTENSION IF NOT EXISTS postgis;

CREATE SCHEMA IF NOT EXISTS incidencias;

CREATE SEQUENCE incidencias.folio_seq START 1;

CREATE TABLE incidencias.brigadas (
    id         uuid PRIMARY KEY,
    tenant_id  varchar(16) NOT NULL,
    tipo       varchar(24) NOT NULL,
    nombre     varchar(120) NOT NULL,
    zona       varchar(80) NOT NULL,
    UNIQUE (tenant_id, tipo),
    CONSTRAINT brigada_tipo_chk CHECK (tipo IN ('BACHE', 'ALUMBRADO', 'AGUA', 'BASURA'))
);

CREATE TABLE incidencias.incidencias (
    id                     uuid PRIMARY KEY,
    tenant_id              varchar(16)    NOT NULL,
    folio                  varchar(32)    NOT NULL,
    tipo                   varchar(24)    NOT NULL,
    estado                 varchar(24)    NOT NULL DEFAULT 'ASIGNADA',
    ciudadano_id           uuid           NOT NULL,
    lat                    numeric(9,6)   NOT NULL,
    lng                    numeric(9,6)   NOT NULL,
    geom                   geography(Point, 4326) GENERATED ALWAYS AS (
                               ST_SetSRID(ST_MakePoint(lng::float8, lat::float8), 4326)::geography
                           ) STORED,
    direccion_referencia   varchar(240)   NOT NULL,
    descripcion            varchar(800)   NOT NULL,
    foto                   bytea,
    foto_content_type      varchar(80),
    brigada_id             uuid           NOT NULL REFERENCES incidencias.brigadas (id),
    creada_en              timestamptz    NOT NULL DEFAULT now(),
    UNIQUE (tenant_id, folio)
);
