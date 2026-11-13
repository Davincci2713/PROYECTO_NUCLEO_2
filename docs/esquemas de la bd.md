# Esquemas de la base de datos

Siete bases en el mismo contenedor. La llave foránea solo existe dentro de su base. `ciudadano_id`, `liquidacion_id`, `transaccion_id`, `tramite_id` y `usuario_id` apuntan a otra base sin restricción. La unicidad de negocio es `tenant_id` más la clave natural (correo, folio, clave catastral, contrato, giro).

Pagos, trámites, ciudad e incidencias tienen además `bitacora` (`id`, `tenant_id`, `ocurrio_en`, `actor_id`, `accion`, `recurso`, `referencia`, `detalle`), sin relación con el resto de tablas.

Cada base tiene su diagrama. `gld_ciudad` va en dos porque tiene catorce tablas. Las columnas son las principales; el detalle está en `database/sql`.

## gld_identity

```mermaid
erDiagram
  USUARIOS {
    uuid id PK
    varchar tenant_id
    varchar nombre
    varchar correo
    varchar password_hash
    varchar rol
    boolean correo_verificado
    boolean totp_activo
  }
```

## gld_catastro

```mermaid
erDiagram
  PREDIOS ||--o{ PERIODOS_PREDIAL : periodos
  PREDIOS {
    uuid id PK
    varchar tenant_id
    varchar clave_catastral
    varchar propietario
    numeric superficie_m2
  }
  PERIODOS_PREDIAL {
    uuid id PK
    uuid predio_id FK
    varchar periodo
    numeric importe_base
    numeric recargo_mora
    boolean pagado
  }
  CONTRATOS_AGUA {
    uuid id PK
    varchar tenant_id
    varchar numero_contrato
    integer lectura_anterior
    integer lectura_actual
    numeric tarifa_m3
    numeric cuota_fija
    boolean pagado
  }
```

## gld_pagos

```mermaid
erDiagram
  LIQUIDACIONES ||--o{ TRANSACCIONES : cobros
  LIQUIDACIONES {
    uuid id PK
    varchar tenant_id
    varchar tipo
    numeric total
    varchar estado
    uuid ciudadano_id
    timestamptz vence_en
    varchar folio_pago
  }
  TRANSACCIONES {
    uuid id PK
    uuid liquidacion_id FK
    varchar medio
    varchar estado
    numeric monto
  }
  MULTAS {
    uuid id PK
    varchar tenant_id
    varchar folio
    varchar placa
    numeric importe
    boolean pagada
  }
  TARIFAS_LICENCIA {
    uuid id PK
    varchar tenant_id
    varchar giro
    numeric tarifa_base
    numeric factor_m2
  }
```

## gld_tramites

```mermaid
erDiagram
  TRAMITES ||--o{ HISTORIAL : historial
  TRAMITES ||--o{ ADJUNTOS : adjuntos
  TRAMITES {
    uuid id PK
    varchar tenant_id
    varchar folio
    varchar tipo
    varchar estado
    uuid ciudadano_id
    uuid liquidacion_id
  }
  HISTORIAL {
    uuid id PK
    uuid tramite_id FK
    varchar estado
    varchar actor
  }
  ADJUNTOS {
    uuid id PK
    uuid tramite_id FK
    varchar nombre
    bytea contenido
  }
```

## gld_documentos

```mermaid
erDiagram
  DOCUMENTOS_OFICIALES {
    uuid id PK
    varchar tenant_id
    varchar folio
    uuid transaccion_id
    uuid tramite_id
    varchar titular
    numeric monto
    varchar sello
    bytea pdf
  }
```

## gld_ciudad

Foros, encuestas y directorio:

```mermaid
erDiagram
  FOROS ||--o{ MENSAJES_FORO : mensajes
  ENCUESTAS ||--o{ OPCIONES_ENCUESTA : opciones
  ENCUESTAS ||--o{ VOTOS : votos
  OPCIONES_ENCUESTA ||--o{ VOTOS : elegida
  PERFILES {
    uuid usuario_id PK
    varchar tenant_id
    varchar nombre
    varchar colonia
  }
  FOROS {
    uuid id PK
    varchar tenant_id
    uuid autor_id
    varchar titulo
  }
  MENSAJES_FORO {
    uuid id PK
    uuid foro_id FK
    uuid autor_id
    boolean es_oficial
  }
  ENCUESTAS {
    uuid id PK
    varchar tenant_id
    varchar pregunta
    uuid creador_id
  }
  OPCIONES_ENCUESTA {
    uuid id PK
    uuid encuesta_id FK
    varchar texto
  }
  VOTOS {
    uuid id PK
    uuid encuesta_id FK
    uuid opcion_id FK
    uuid usuario_id
  }
  PYMES {
    uuid id PK
    varchar tenant_id
    uuid dueno_id
    varchar nombre
    varchar giro
  }
```

Chat, alertas y agenda. Un voto y una inscripción son únicos por ciudadano.

```mermaid
erDiagram
  SALAS ||--o{ MENSAJES_CHAT : mensajes
  EVENTOS ||--o{ INSCRIPCIONES : asistentes
  SALAS {
    uuid id PK
    varchar tenant_id
    varchar nombre
    varchar tema
  }
  MENSAJES_CHAT {
    uuid id PK
    uuid sala_id FK
    uuid autor_id
    varchar texto
  }
  PALABRAS_BLOQUEADAS {
    varchar palabra PK
  }
  PREFERENCIAS_ALERTA {
    uuid usuario_id PK
    varchar tenant_id
    varchar telefono
    text temas
  }
  ALERTAS_ENVIADAS {
    uuid id PK
    varchar tenant_id
    uuid usuario_id
    varchar tema
  }
  EVENTOS {
    uuid id PK
    varchar tenant_id
    varchar titulo
    varchar lugar
    uuid creador_id
  }
  INSCRIPCIONES {
    uuid id PK
    uuid evento_id FK
    uuid usuario_id
  }
```

## gld_incidencias

PostGIS solo en esta base. `geom` se genera a partir de `lat` y `lng`.

```mermaid
erDiagram
  BRIGADAS ||--o{ INCIDENCIAS : atiende
  BRIGADAS {
    uuid id PK
    varchar tenant_id
    varchar tipo
    varchar nombre
    varchar zona
  }
  INCIDENCIAS {
    uuid id PK
    varchar tenant_id
    varchar folio
    varchar tipo
    varchar estado
    uuid ciudadano_id
    numeric lat
    numeric lng
    uuid brigada_id FK
  }
```
