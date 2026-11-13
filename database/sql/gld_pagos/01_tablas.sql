-- Base gld_pagos.

CREATE SCHEMA IF NOT EXISTS pagos;

CREATE TABLE pagos.tarifas_licencia (
    id           uuid PRIMARY KEY,
    tenant_id    varchar(16)   NOT NULL,
    giro         varchar(40)   NOT NULL,
    tarifa_base  numeric(14,2) NOT NULL,
    factor_m2    numeric(14,2) NOT NULL,
    UNIQUE (tenant_id, giro)
);

CREATE TABLE pagos.multas (
    id                 uuid PRIMARY KEY,
    tenant_id          varchar(16)   NOT NULL,
    folio              varchar(32)   NOT NULL,
    placa              varchar(16)   NOT NULL,
    descripcion        varchar(240)  NOT NULL,
    importe            numeric(14,2) NOT NULL,
    fecha_infraccion   date          NOT NULL,
    pagada             boolean       NOT NULL DEFAULT false,
    UNIQUE (tenant_id, folio)
);

CREATE SEQUENCE pagos.folio_seq START 1;

CREATE TABLE pagos.liquidaciones (
    id            uuid PRIMARY KEY,
    tenant_id     varchar(16)   NOT NULL,
    tipo          varchar(24)   NOT NULL,
    referencia    varchar(120)  NOT NULL,
    concepto      varchar(240)  NOT NULL,
    subtotal      numeric(14,2) NOT NULL,
    recargos      numeric(14,2) NOT NULL DEFAULT 0,
    descuento     numeric(14,2) NOT NULL DEFAULT 0,
    total         numeric(14,2) NOT NULL,
    estado        varchar(16)   NOT NULL,
    ciudadano_id  uuid,
    desglose      jsonb         NOT NULL DEFAULT '[]'::jsonb,
    creada_en     timestamptz   NOT NULL DEFAULT now(),
    vence_en      timestamptz   NOT NULL,
    folio_pago    varchar(32),
    CONSTRAINT liquidacion_tipo_chk CHECK (tipo IN ('PREDIAL', 'AGUA', 'LICENCIA', 'MULTA')),
    CONSTRAINT liquidacion_estado_chk CHECK (estado IN ('ACTIVA', 'PAGADA', 'EXPIRADA', 'CANCELADA'))
);

CREATE UNIQUE INDEX uq_liquidacion_activa
    ON pagos.liquidaciones (tenant_id, tipo, referencia)
    WHERE estado = 'ACTIVA';

CREATE TABLE pagos.transacciones (
    id               uuid PRIMARY KEY,
    liquidacion_id   uuid          NOT NULL REFERENCES pagos.liquidaciones (id),
    medio            varchar(16)   NOT NULL,
    estado           varchar(16)   NOT NULL,
    monto            numeric(14,2) NOT NULL,
    concepto         varchar(240)  NOT NULL,
    motivo_rechazo   varchar(240),
    creada_en        timestamptz   NOT NULL DEFAULT now(),
    CONSTRAINT transaccion_medio_chk CHECK (medio IN ('TARJETA', 'PSE')),
    CONSTRAINT transaccion_estado_chk CHECK (estado IN ('AUTORIZADA', 'RECHAZADA'))
);
