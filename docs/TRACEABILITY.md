# Trazabilidad del MVP (épicas 1 y 3)

Leyenda de estado de implementación: la columna Pantalla la cubre `gld-frontend` y la columna API la cubre `gld-backend`. Esta matriz es el criterio de cierre del corte.

| ID | Historia | API | Pantalla | Regla visible |
| --- | --- | --- | --- | --- |
| HU-01 | Pago de predial | `GET /api/v1/predios/{clave}` · `POST /api/v1/pagos/liquidaciones/predial` · `POST /api/v1/pagos/liquidaciones/{id}/cobrar` | `/app/predial` | 2 % en línea, rechazo deja la liquidación activa |
| HU-02 | Pago de agua | `GET /api/v1/agua/contratos/{numero}` · `POST /api/v1/pagos/liquidaciones/agua` · `GET /api/v1/agua/contratos` | `/app/agua` · `/backoffice/agua` | el pago marca el contrato al corriente |
| HU-03 | Licencia de funcionamiento | `POST /api/v1/pagos/cotizaciones/licencia` · `POST /api/v1/tramites/licencias-funcionamiento` · dictamen · cobro | `/app/licencias` · `/backoffice/bandeja` | el documento nace tras aprobación y pago |
| HU-04 | Multas | `GET /api/v1/pagos/multas` · `POST /api/v1/pagos/liquidaciones/multa` | `/app/multas` | 50 % en los primeros 10 días |
| HU-05 | Licencia de construcción | `POST /api/v1/tramites/licencias-construccion` | `/app/construccion` | radica sin pasarela |
| HU-06 | Seguimiento por folio | `GET /api/v1/tramites/folio/{folio}` · `GET /api/v1/tramites/mios` | `/app/seguimiento` | la línea de tiempo cambia con el dictamen |
| HU-07 | Recibo, sello y QR | `GET /api/v1/documentos/{id}` · `GET /api/v1/documentos/{id}/pdf` · `GET /api/v1/documentos/verificar/{folio}` | `/app/recibos/{id}` · `/verificar/{folio}` | folio copiable |
| HU-08 | Medios de pago | cuerpo `medio` de `/cobrar` | paso de pago de cada trámite | concepto e importe antes de confirmar |
| RF-07 | Temporizador 72 h | estado `EXPIRADA` al cobrar o al crear | mensaje en el paso de pago | obliga a recalcular |
| HU-18 | Registro, verificación y 2FA | `/api/v1/auth/**` · `GET/PUT /api/v1/ciudad/perfiles/me` | `/registro` · `/verificar` · `/login` · `/app/perfil` | TOTP |
| HU-19 | Foros y respuesta oficial | `/api/v1/ciudad/foros/**` | `/app/ciudad/foros` | la respuesta de servidor va etiquetada |
| HU-20 | Encuestas | `/api/v1/ciudad/encuestas/**` | `/app/ciudad/encuestas` | un voto por cuenta |
| HU-21 | Directorio PyME | `/api/v1/ciudad/pymes/**` | `/app/ciudad/directorio` | alta y búsqueda |
| HU-22 | Salas de chat | `/api/v1/ciudad/salas/**` | `/app/ciudad/chat` | moderación de lenguaje |
| HU-23 | Alertas | `/api/v1/ciudad/alertas/**` | `/app/ciudad/alertas` | SMS simulado y preferencias |
| HU-24 | Incidencias | `/api/v1/incidencias/**` | `/app/incidencias/nueva` | dirección precargada y editable |
| HU-25 | Agenda | `/api/v1/ciudad/eventos/**` | `/app/ciudad/eventos` | inscripción |
| H-01..H-06 | Usabilidad | errores 400 con `campo` y `ejemplo` | componentes de trámite y mapa | ver `frontend/.cursorrules` |
| RNF-12 | OpenAPI 3 | `/v3/api-docs` en cada servicio | — | la operación cita la HU |
| RNF-14 | Bitácora | tabla `bitacora` en la base del servicio | — | alta en pago, trámite, voto e incidencia |

Fuera de esta tabla no hay trabajo de producto en el MVP.
