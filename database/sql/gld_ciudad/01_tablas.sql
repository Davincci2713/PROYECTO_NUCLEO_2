-- Base gld_ciudad.

CREATE SCHEMA IF NOT EXISTS ciudad;

CREATE TABLE ciudad.perfiles (
    usuario_id  uuid PRIMARY KEY,
    tenant_id   varchar(16)  NOT NULL,
    nombre      varchar(160) NOT NULL,
    biografia   varchar(500) NOT NULL DEFAULT '',
    colonia     varchar(120) NOT NULL DEFAULT ''
);

CREATE TABLE ciudad.foros (
    id            uuid PRIMARY KEY,
    tenant_id     varchar(16)  NOT NULL,
    autor_id      uuid         NOT NULL,
    titulo        varchar(180) NOT NULL,
    cuerpo        varchar(2000) NOT NULL,
    creado_en     timestamptz  NOT NULL DEFAULT now()
);

CREATE TABLE ciudad.mensajes_foro (
    id           uuid PRIMARY KEY,
    foro_id      uuid          NOT NULL REFERENCES ciudad.foros (id),
    autor_id     uuid          NOT NULL,
    autor_nombre varchar(160)  NOT NULL,
    cuerpo       varchar(2000) NOT NULL,
    es_oficial   boolean       NOT NULL DEFAULT false,
    creado_en    timestamptz   NOT NULL DEFAULT now()
);

CREATE TABLE ciudad.encuestas (
    id          uuid PRIMARY KEY,
    tenant_id   varchar(16)   NOT NULL,
    pregunta    varchar(300)  NOT NULL,
    cierra_en   timestamptz   NOT NULL,
    creador_id  uuid          NOT NULL
);

CREATE TABLE ciudad.opciones_encuesta (
    id           uuid PRIMARY KEY,
    encuesta_id  uuid         NOT NULL REFERENCES ciudad.encuestas (id),
    texto        varchar(180) NOT NULL
);

CREATE TABLE ciudad.votos (
    id           uuid PRIMARY KEY,
    encuesta_id  uuid        NOT NULL REFERENCES ciudad.encuestas (id),
    opcion_id    uuid        NOT NULL REFERENCES ciudad.opciones_encuesta (id),
    usuario_id   uuid        NOT NULL,
    creado_en    timestamptz NOT NULL DEFAULT now(),
    UNIQUE (encuesta_id, usuario_id)
);

CREATE TABLE ciudad.pymes (
    id           uuid PRIMARY KEY,
    tenant_id    varchar(16)   NOT NULL,
    dueno_id     uuid          NOT NULL,
    nombre       varchar(160)  NOT NULL,
    giro         varchar(80)   NOT NULL,
    descripcion  varchar(500)  NOT NULL,
    telefono     varchar(20)   NOT NULL,
    colonia      varchar(120)  NOT NULL,
    creado_en    timestamptz   NOT NULL DEFAULT now()
);

CREATE TABLE ciudad.salas (
    id          uuid PRIMARY KEY,
    tenant_id   varchar(16)  NOT NULL,
    nombre      varchar(120) NOT NULL,
    tema        varchar(160) NOT NULL
);

CREATE TABLE ciudad.mensajes_chat (
    id           uuid PRIMARY KEY,
    sala_id      uuid         NOT NULL REFERENCES ciudad.salas (id),
    autor_id     uuid         NOT NULL,
    autor_nombre varchar(160) NOT NULL,
    texto        varchar(500) NOT NULL,
    creado_en    timestamptz  NOT NULL DEFAULT now()
);

CREATE TABLE ciudad.palabras_bloqueadas (
    palabra varchar(40) PRIMARY KEY
);

CREATE TABLE ciudad.preferencias_alerta (
    usuario_id  uuid PRIMARY KEY,
    tenant_id   varchar(16) NOT NULL,
    telefono    varchar(20) NOT NULL,
    temas       text[]      NOT NULL DEFAULT ARRAY[]::text[]
);

CREATE TABLE ciudad.alertas_enviadas (
    id          uuid PRIMARY KEY,
    tenant_id   varchar(16)  NOT NULL,
    usuario_id  uuid         NOT NULL,
    telefono    varchar(20)  NOT NULL,
    tema        varchar(40)  NOT NULL,
    mensaje     varchar(320) NOT NULL,
    enviada_en  timestamptz  NOT NULL DEFAULT now()
);

CREATE TABLE ciudad.eventos (
    id           uuid PRIMARY KEY,
    tenant_id    varchar(16)   NOT NULL,
    titulo       varchar(180)  NOT NULL,
    descripcion  varchar(800)  NOT NULL,
    lugar        varchar(180)  NOT NULL,
    inicia_en    timestamptz   NOT NULL,
    creador_id   uuid          NOT NULL
);

CREATE TABLE ciudad.inscripciones (
    id          uuid PRIMARY KEY,
    evento_id   uuid        NOT NULL REFERENCES ciudad.eventos (id),
    usuario_id  uuid        NOT NULL,
    creado_en   timestamptz NOT NULL DEFAULT now(),
    UNIQUE (evento_id, usuario_id)
);
