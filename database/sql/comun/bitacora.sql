-- Bitácora del servicio que escribe. Vive en esta base, no en una base compartida.
-- El municipio demo 05021 es solo el valor de tenant_id.

CREATE TABLE public.bitacora (
    id          bigserial PRIMARY KEY,
    tenant_id   varchar(16)  NOT NULL,
    ocurrio_en  timestamptz  NOT NULL DEFAULT now(),
    actor_id    uuid,
    accion      varchar(80)  NOT NULL,
    recurso     varchar(80)  NOT NULL,
    referencia  varchar(80),
    detalle     varchar(500) NOT NULL
);
