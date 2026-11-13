-- Base gld_catastro.

CREATE SCHEMA IF NOT EXISTS catastro;

CREATE TABLE catastro.predios (
    id               uuid PRIMARY KEY,
    tenant_id        varchar(16)   NOT NULL,
    clave_catastral  varchar(20)   NOT NULL,
    propietario      varchar(160)  NOT NULL,
    direccion        varchar(240)  NOT NULL,
    superficie_m2    numeric(12,2) NOT NULL,
    UNIQUE (tenant_id, clave_catastral)
);

CREATE TABLE catastro.periodos_predial (
    id                 uuid PRIMARY KEY,
    predio_id          uuid          NOT NULL REFERENCES catastro.predios (id),
    periodo            varchar(16)   NOT NULL,
    importe_base       numeric(14,2) NOT NULL,
    recargo_mora       numeric(14,2) NOT NULL DEFAULT 0,
    fecha_vencimiento  date          NOT NULL,
    pagado             boolean       NOT NULL DEFAULT false,
    UNIQUE (predio_id, periodo)
);

CREATE TABLE catastro.contratos_agua (
    id                 uuid PRIMARY KEY,
    tenant_id          varchar(16)   NOT NULL,
    numero_contrato    varchar(32)   NOT NULL,
    titular            varchar(160)  NOT NULL,
    direccion          varchar(240)  NOT NULL,
    periodo            varchar(16)   NOT NULL,
    lectura_anterior   integer       NOT NULL,
    lectura_actual     integer       NOT NULL,
    tarifa_m3          numeric(14,2) NOT NULL,
    cuota_fija         numeric(14,2) NOT NULL,
    pagado             boolean       NOT NULL DEFAULT false,
    UNIQUE (tenant_id, numero_contrato),
    CONSTRAINT agua_lecturas_chk CHECK (lectura_actual >= lectura_anterior)
);
