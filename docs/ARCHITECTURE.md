# Arquitectura del MVP — Gobiernos Locales Digitales

Fuente de dominio: `especificacion-microservicios-gld.md` (raíz del monorepo).
Contrato HTTP: `docs/FRONTEND_API_CONTRACT.md`.
Trazabilidad: `docs/TRACEABILITY.md`.
Reglas de los agentes: `.cursorrules`, `backend/.cursorrules`, `frontend/.cursorrules` y `.cursor/rules/architect.mdc`.
Diagramas, cada uno en su archivo: [stack tecnológico](<stack tecnologico.md>), [arquitectura](<diagrama de arquitectura.md>), [casos de uso](<casos de uso.md>), [clases](<diagrama de clases.md>) y [esquemas](<esquemas de la bd.md>).

## Stack

- **Backend:** Java 21 y Spring Boot 3.4. Un proceso por contexto acotado, más un gateway. No hay otro framework de servidor.
- **Frontend:** Next.js 15 con TypeScript. La especificación deja la capa web en React / Next.js; esta es la elección del arquitecto para el portal ciudadano y la bandeja del servidor.
- **Base de datos:** PostgreSQL 16 con PostGIS, una base por microservicio dentro del mismo contenedor. No se comparten tablas ni hay llaves foráneas entre bases. El identificador de municipio `05021` es una columna en cada base, no una tabla maestra compartida.

## Alcance

Entra el MVP de:

- Épica 1, Ventanilla única y trámites: HU-01 a HU-08.
- Épica 3, Ciudad digital y red social cívica: HU-18 a HU-25.

Quedan fuera la épica 2 (HU-09 a HU-17) y la épica 4 (HU-26 a HU-30).

## Monorepo

```
especificacion-microservicios-gld.md
docs/                 contrato, arquitectura, diagramas, trazabilidad
database/sql          esquema, semilla y la base reproducible
backend/              un módulo Maven por microservicio
frontend/             portal ciudadano y bandeja mínima
.cursor/agents        gld-backend y gld-frontend
```

`docker compose up -d` en la raíz levanta el contenedor. Para recrear las bases desde los scripts hay que borrar el volumen: `docker compose down -v` y volver a subir.

## Contextos del MVP

| Servicio | Puerto | Base | Historias |
| --- | --- | --- | --- |
| gld-gateway | 8080 | — | borde único del portal |
| gld-identity | 8081 | gld_identity | HU-18, acceso del resto |
| gld-catastro | 8082 | gld_catastro | consulta de HU-01 y HU-02 |
| gld-pagos | 8083 | gld_pagos | HU-01, HU-02, HU-03, HU-04, HU-08 |
| gld-tramites | 8084 | gld_tramites | HU-03, HU-05, HU-06 y subsanación |
| gld-documentos | 8085 | gld_documentos | HU-07 |
| gld-ciudad-digital | 8086 | gld_ciudad | HU-19 a HU-23 y HU-25 |
| gld-incidencias | 8087 | gld_incidencias | HU-24 |

`gld-common` es un jar de plataforma (JWT, tenant, error API). No contiene reglas de negocio.

## Decisiones del MVP

La especificación objetivo usa Kong, Keycloak, Kafka, MinIO, schema-per-tenant y una pasarela PCI real. El MVP las sustituye por equivalentes ejecutables y deja la pieza objetivo escrita aquí, para no fingir un componente que no está operando.

| Objetivo de la especificación | Decisión del MVP | Motivo |
| --- | --- | --- |
| Kong | Spring Cloud Gateway | mismo borde, `X-Tenant-ID` y CORS, sin un producto extra |
| Keycloak + OIDC | `gld-identity` con JWT y TOTP | HU-18 queda demostrable en local |
| Kafka y SAGA | orquestación síncrona desde pagos y trámites, más bitácora | un solo municipio demo no justifica el broker |
| schema-per-tenant | columna `tenant_id` y un municipio `05021` | el aislamiento por schema queda como evolución; toda fila ya carga el inquilino |
| MinIO | `bytea` en PostgreSQL | adjuntos y PDF del MVP |
| PostGIS operativo | imagen PostGIS y columna `geom` generada | HU-24 guarda el punto; la consulta de negocio usa lat/lng |
| Pasarela bancaria | simulador `TARJETA` o `PSE` con `AUTORIZADO` o `RECHAZADO` | no se almacenan datos de tarjeta (RNF-07) |
| PSC de firma | sello HMAC-SHA256 de Tesorería demo | el recibo igual lleva folio, transacción, sello y QR |
| SMS | registro en `ciudad.alertas_enviadas` | no hay proveedor SMS |
| Cobertura ≥ 80 % (RNF-10) | JaCoCo exige ≥ 80 % de líneas en cada módulo JAR, excluyendo solo las clases *Application, y `mvn test` lo comprueba | el umbral entra en este corte y el build falla si un módulo baja de 80 % |

## Reglas que sí entran

- Predial: suma de base + recargo por periodo, y 2 % de descuento sobre el subtotal si el pago es en línea.
- Agua: `(lecturaActual - lecturaAnterior) * tarifaM3 + cuotaFija`.
- Licencia: `tarifaBase + superficieM2 * factorM2`. Se cotiza al capturar y se cobra después de la aprobación técnica.
- Multa: 50 % si la infracción tiene 10 días o menos.
- Liquidación activa vence a las 72 horas, pasa a `EXPIRADA` y obliga a recalcular.
- Obra: radica expediente con adjunto y sin cargo en pasarela.
- Subsanación: el funcionario observa, el estado pasa a `POR_SUBSANAR` y el ciudadano carga documentos con el mismo folio.
- Encuesta: un voto por ciudadano.
- Chat: rechaza el mensaje si contiene una palabra de `ciudad.palabras_bloqueadas`.
- Incidencia: el clic del mapa produce una dirección editable y una brigada según el tipo.
- Bitácora append-only en la tabla `bitacora` de la base del servicio que escribe (`gld_pagos`, `gld_tramites`, `gld_ciudad`, `gld_incidencias`). No hay borrado (RNF-14, retención objetivo de 5 años).

## Tenencia y seguridad

- Municipio demo: `05021`, encabezado `X-Tenant-ID: 05021`.
- Secreto JWT local, el mismo en todos los servicios: `gld-dev-secret-cambia-en-produccion-32b`.
- Secreto interno entre servicios: encabezado `X-Internal-Token: gld-dev-internal`.
- Contraseña de la semilla: `Demo123!`.
- CORS del gateway: `http://localhost:3000`.

## Arranque

1. `docker compose up -d`
2. En `backend/`, `mvn test` y luego cada servicio con `mvn -pl <modulo> spring-boot:run`
3. En `frontend/`, `npm install` y `npm run dev`

El gateway es el único origen que el portal debe llamar.
