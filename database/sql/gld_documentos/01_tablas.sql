-- Base gld_documentos. transaccion_id y tramite_id no referencian otras bases.

CREATE SCHEMA IF NOT EXISTS documentos;

CREATE SEQUENCE documentos.folio_seq START 1;

CREATE TABLE documentos.documentos_oficiales (
    id               uuid PRIMARY KEY,
    tenant_id        varchar(16)   NOT NULL,
    folio            varchar(32)   NOT NULL,
    tipo             varchar(40)   NOT NULL,
    transaccion_id   uuid,
    tramite_id       uuid,
    titular          varchar(160)  NOT NULL,
    concepto         varchar(240)  NOT NULL,
    monto            numeric(14,2) NOT NULL,
    sello            varchar(128)  NOT NULL,
    qr_payload       varchar(300)  NOT NULL,
    emitido_en       timestamptz   NOT NULL DEFAULT now(),
    pdf              bytea         NOT NULL,
    UNIQUE (tenant_id, folio)
);
