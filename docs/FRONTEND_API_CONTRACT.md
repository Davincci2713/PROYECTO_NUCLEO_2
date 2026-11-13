# Contrato HTTP del portal GLD

Base única: `http://localhost:8080`. El portal no llama a los puertos 8081-8087.

Encabezados en toda ruta que no sea pública:

- `Authorization: Bearer <accessToken>`
- `X-Tenant-ID: 05021`
- `Content-Type: application/json`

Rutas públicas (sin Bearer): `POST /api/v1/auth/registro`, `POST /api/v1/auth/verificar-correo`, `POST /api/v1/auth/login`, `POST /api/v1/auth/2fa/verificar`, `GET /api/v1/documentos/verificar/{folio}`, `GET /actuator/health`.

El pre-token de 2FA solo sirve para `POST /api/v1/auth/2fa/verificar`.

Dinero: número JSON con dos decimales, redondeo half-up. Fechas `YYYY-MM-DD`. Instantes ISO-8601 con zona.

## Error

Todas las fallas de negocio usan este cuerpo. El HTTP es el de `status`.

```json
{
  "titulo": "Datos inválidos",
  "status": 400,
  "detalle": "Clave catastral incorrecta. Formato esperado: 05-021-0147-003",
  "campo": "claveCatastral",
  "ejemplo": "05-021-0147-003",
  "codigo": "CLAVE_CATASTRAL_INVALIDA"
}
```

`campo` y `ejemplo` van cuando el fallo es de un dato capturado (H-04). Códigos usados: `CLAVE_CATASTRAL_INVALIDA`, `PREDIOS_NO_ENCONTRADOS`, `PERIODO_NO_DISPONIBLE`, `CONTRATO_NO_ENCONTRADO`, `SALDO_AL_CORRIENTE`, `GIRO_DESCONOCIDO`, `MULTA_NO_ENCONTRADA`, `LIQUIDACION_NO_ENCONTRADA`, `LIQUIDACION_EXPIRADA`, `LIQUIDACION_NO_ACTIVA`, `MEDIO_INVALIDO`, `CORREO_YA_REGISTRADO`, `CORREO_NO_VERIFICADO`, `CODIGO_INVALIDO`, `CREDENCIALES_INVALIDAS`, `TOTP_INVALIDO`, `TRAMITE_NO_ENCONTRADO`, `ESTADO_NO_PERMITE`, `OBSERVACION_OBLIGATORIA`, `ADJUNTO_OBLIGATORIO`, `YA_VOTO`, `LENGUAJE_NO_PERMITIDO`, `ENCUESTA_CERRADA`, `NO_AUTORIZADO`.

`401` sin token o token vencido. `403` con token de un rol que no puede la operación. `NO_AUTORIZADO` acompaña al 403.

## Identidad (HU-18)

`POST /api/v1/auth/registro`

```json
{ "nombre": "Luis Pérez", "correo": "luis@correo.test", "password": "Demo123!", "telefono": "5510000000" }
```

`201`

```json
{ "id": "uuid", "correo": "luis@correo.test", "codigoVerificacion": "482193" }
```

El código de 6 dígitos vuelve en la respuesta porque el MVP no envía correo. La contraseña tiene al menos 8 caracteres. El rol creado es `CIUDADANO` y también se crea el perfil de ciudad.

`POST /api/v1/auth/verificar-correo`

```json
{ "correo": "luis@correo.test", "codigo": "482193" }
```

`204` vacío. Código distinto: `400 CODIGO_INVALIDO`, campo `codigo`, ejemplo `482193`.

`POST /api/v1/auth/login`

```json
{ "correo": "ciudadano@gld.demo", "password": "Demo123!" }
```

Sin 2FA, `200`:

```json
{
  "accessToken": "jwt",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "requiere2fa": false,
  "usuario": {
    "id": "11111111-1111-4111-8111-111111111111",
    "nombre": "María López Hernández",
    "correo": "ciudadano@gld.demo",
    "rol": "CIUDADANO",
    "tenantId": "05021"
  }
}
```

Con TOTP activo, `200` y sin `usuario` de sesión completa:

```json
{ "accessToken": "pre-token", "tokenType": "Bearer", "expiresIn": 300, "requiere2fa": true }
```

Correo no verificado: `403 CORREO_NO_VERIFICADO`. Credenciales mal: `401 CREDENCIALES_INVALIDAS`, sin decir si falló el correo o la clave.

`POST /api/v1/auth/2fa/verificar` con el pre-token:

```json
{ "codigo": "123456" }
```

`200` con la misma forma del login completo. Código mal: `400 TOTP_INVALIDO`, campo `codigo`, ejemplo `123456`.

`POST /api/v1/auth/2fa/activar` (sesión completa) `200`:

```json
{ "secreto": "BASE32", "otpauthUrl": "otpauth://totp/GLD:correo?secret=BASE32&issuer=GLD" }
```

`POST /api/v1/auth/2fa/confirmar` `{ "codigo": "123456" }` `204`. A partir de ahí el login exige el segundo factor.

`GET /api/v1/auth/me` `200` devuelve el objeto `usuario`.

Claims del JWT de sesión: `sub` (uuid), `correo`, `nombre`, `rol`, `tenantId`, `mfa` (`ok` o `pending`). Vigencia de sesión 3600 s. El pre-token dura 300 s y `mfa=pending`.

## Perfil

`GET /api/v1/ciudad/perfiles/me`

```json
{
  "usuarioId": "11111111-1111-4111-8111-111111111111",
  "nombre": "María López Hernández",
  "biografia": "Vecina del centro.",
  "colonia": "Centro",
  "totpActivo": false
}
```

`PUT /api/v1/ciudad/perfiles/me` `{ "biografia": "...", "colonia": "..." }` devuelve el mismo objeto. `totpActivo` lo informa identidad; ciudad digital lo consulta o lo deja en el JWT. Si ciudad no puede leer el secreto, el campo sale del claim y de un `GET` interno. Implementación aceptada: identity expone `GET /api/v1/auth/me` y el portal combina perfil + `me`. El `GET` de perfil incluye `totpActivo` llamando a identity con el token interno y el id, o identity escribe la bandera. Contrato del portal: el campo viene en `GET /api/v1/ciudad/perfiles/me`.

## Catastro y predial (HU-01)

Clave con formato `\d{2}-\d{3}-\d{4}-\d{3}`. Si no cumple: `400 CLAVE_CATASTRAL_INVALIDA`. Si cumple y no existe: `404 PREDIOS_NO_ENCONTRADOS`, campo `claveCatastral`, ejemplo `05-021-0147-003`.

`GET /api/v1/predios/05-021-0147-003`

```json
{
  "claveCatastral": "05-021-0147-003",
  "propietario": "María López Hernández",
  "direccion": "Av. Reforma 120, Centro",
  "superficieM2": 180.00,
  "periodos": [
    {
      "periodo": "2025-6",
      "importeBase": 1500.00,
      "recargoMora": 180.00,
      "importePeriodo": 1680.00,
      "fechaVencimiento": "2026-09-08",
      "vencido": true,
      "pagado": false
    },
    {
      "periodo": "2026-1",
      "importeBase": 1500.00,
      "recargoMora": 0.00,
      "importePeriodo": 1500.00,
      "fechaVencimiento": "2026-11-27",
      "vencido": false,
      "pagado": false
    }
  ]
}
```

`vencido` es `fechaVencimiento < hoy`. `importePeriodo = importeBase + recargoMora`. Las fechas del ejemplo dependen de la semilla (`CURRENT_DATE`); el portal muestra las que responda el API.

`POST /api/v1/pagos/liquidaciones/predial`

```json
{ "claveCatastral": "05-021-0147-003", "periodos": ["2025-6", "2026-1"] }
```

Fixture con la semilla, ambos periodos:

- subtotal `3180.00` (suma de importePeriodo)
- recargos `180.00`
- descuento `63.60` (2 % del subtotal)
- total `3116.40`

```json
{
  "id": "uuid",
  "tipo": "PREDIAL",
  "referencia": "05-021-0147-003:2025-6,2026-1",
  "concepto": "Impuesto predial 2025-6, 2026-1",
  "estado": "ACTIVA",
  "subtotal": 3180.00,
  "recargos": 180.00,
  "descuento": 63.60,
  "descuentoPorcentaje": 2,
  "total": 3116.40,
  "venceEn": "2026-11-10T18:00:00Z",
  "desglose": [
    { "periodo": "2025-6", "importeBase": 1500.00, "recargoMora": 180.00, "importe": 1680.00 },
    { "periodo": "2026-1", "importeBase": 1500.00, "recargoMora": 0.00, "importe": 1500.00 }
  ]
}
```

Periodo ya pagado o desconocido: `409 PERIODO_NO_DISPONIBLE`. Si ya hay una liquidación `ACTIVA` de la misma referencia y no venció, se devuelve esa, sin duplicar. Si venció, se marca `EXPIRADA` y se crea otra releyendo el padrón.

`venceEn = creadaEn + 72 horas`.

## Agua (HU-02)

`GET /api/v1/agua/contratos/CTA-1001`

```json
{
  "numeroContrato": "CTA-1001",
  "titular": "María López Hernández",
  "direccion": "Av. Reforma 120, Centro",
  "periodo": "2026-2",
  "lecturaAnterior": 100,
  "lecturaActual": 128,
  "consumoM3": 28,
  "tarifaM3": 18.50,
  "cuotaFija": 45.00,
  "importe": 563.00,
  "pagado": false
}
```

`importe = consumoM3 * tarifaM3 + cuotaFija`. No hay descuento en línea. `404 CONTRATO_NO_ENCONTRADO`, campo `numeroContrato`, ejemplo `CTA-1001`.

`GET /api/v1/agua/contratos` lo usa el rol `SERVIDOR_PUBLICO` o `ADMINISTRADOR`. `200` es un arreglo de los mismos objetos. Un ciudadano recibe `403`.

`POST /api/v1/pagos/liquidaciones/agua` `{ "numeroContrato": "CTA-1001" }` devuelve la liquidación. `tipo=AGUA`, `descuento=0`, `descuentoPorcentaje=0`, `recargos=0`, `subtotal=total=563.00` con la semilla, `concepto` `Servicio de agua CTA-1001 periodo 2026-2`. Si `pagado=true`: `409 SALDO_AL_CORRIENTE`.

Al autorizar el cobro, catastro marca `pagado=true`.

## Licencia de funcionamiento (HU-03)

`GET /api/v1/pagos/cotizaciones/giros` `200`:

```json
[
  { "giro": "ABARROTES", "tarifaBase": 800.00, "factorM2": 12.00 },
  { "giro": "RESTAURANTE", "tarifaBase": 1400.00, "factorM2": 18.00 },
  { "giro": "OFICINA", "tarifaBase": 600.00, "factorM2": 10.00 }
]
```

`POST /api/v1/pagos/cotizaciones/licencia` `{ "giro": "ABARROTES", "superficieM2": 40 }` `200`:

```json
{ "giro": "ABARROTES", "tarifaBase": 800.00, "factorM2": 12.00, "superficieM2": 40.00, "total": 1280.00 }
```

`total = tarifaBase + superficieM2 * factorM2`. Giro fuera de catálogo: `400 GIRO_DESCONOCIDO`, campo `giro`, ejemplo `ABARROTES`. Superficie menor o igual a 0: `400`, campo `superficieM2`, ejemplo `40`.

`POST /api/v1/tramites/licencias-funcionamiento`

```json
{ "nombreComercial": "Abarrotes Luna", "giro": "ABARROTES", "superficieM2": 40 }
```

`201` trámite en `EN_REVISION`, sin `liquidacionId`. El `payload` guarda la cotización. Ver forma de trámite más abajo.

Dictamen `APROBAR` crea la liquidación `LICENCIA` por ese total, guarda `liquidacionId` y pasa el trámite a `APROBADO_PENDIENTE_PAGO`. El cobro autorizado lo pasa a `PAGADO` y emite el documento.

## Multas (HU-04)

`GET /api/v1/pagos/multas?placa=ABC-123` o `?folio=MUL-2026-001`. Hace falta uno de los dos. Si faltan ambos: `400`, campo `placa`, ejemplo `ABC-123`.

```json
{
  "resultados": [
    {
      "folio": "MUL-2026-001",
      "placa": "ABC-123",
      "descripcion": "Exceso de velocidad en zona escolar",
      "importe": 800.00,
      "fechaInfraccion": "2026-11-04",
      "dentroDeProntoPago": true,
      "descuentoProntoPago": 400.00,
      "totalAPagar": 400.00,
      "pagada": false
    }
  ]
}
```

`dentroDeProntoPago` es verdadero cuando `hoy - fechaInfraccion <= 10`. Entonces `descuentoProntoPago = importe * 0.50` y `totalAPagar = importe - descuentoProntoPago`. Si no, descuento `0` y total igual al importe. La multa `MUL-2025-014` de la semilla no lleva descuento (`1200.00`). Lista vacía si la placa no tiene filas: `200` con `resultados: []`. Folio inexistente: `404 MULTA_NO_ENCONTRADA`, campo `folio`, ejemplo `MUL-2026-001`.

`POST /api/v1/pagos/liquidaciones/multa` `{ "folio": "MUL-2026-001" }` crea la liquidación con el total ya descontado. `concepto` incluye el folio y, si aplica, el texto `pronto pago 50 %`.

## Cobro (HU-08 y RF-07)

`GET /api/v1/pagos/liquidaciones/{id}` devuelve la liquidación. Si `venceEn` ya pasó y sigue `ACTIVA`, la respuesta la entrega como `EXPIRADA` y así queda persistida.

`POST /api/v1/pagos/liquidaciones/{id}/cobrar`

```json
{ "medio": "TARJETA", "simular": "AUTORIZADO" }
```

`medio`: `TARJETA` o `PSE`. Otro valor: `400 MEDIO_INVALIDO`, campo `medio`, ejemplo `TARJETA`. `simular`: `AUTORIZADO` o `RECHAZADO`.

El portal muestra `concepto` y `total` de la liquidación antes de enviar este cuerpo. No se envían números de tarjeta.

Autorizado, `200`:

```json
{
  "estadoPago": "AUTORIZADO",
  "liquidacionId": "uuid",
  "estadoLiquidacion": "PAGADA",
  "folio": "PAG-2026-000001",
  "transaccionId": "uuid",
  "documentoId": "uuid",
  "concepto": "Impuesto predial 2025-6, 2026-1",
  "total": 3116.40,
  "medio": "TARJETA"
}
```

Rechazado, `200`. La liquidación sigue `ACTIVA`:

```json
{
  "estadoPago": "RECHAZADO",
  "liquidacionId": "uuid",
  "estadoLiquidacion": "ACTIVA",
  "motivo": "La pasarela rechazó el cargo. La liquidación sigue activa; puedes reintentar con otro medio.",
  "concepto": "Impuesto predial 2025-6, 2026-1",
  "total": 3116.40,
  "medio": "PSE"
}
```

Vencida, `409 LIQUIDACION_EXPIRADA`, detalle `La liquidación superó las 72 horas y fue marcada EXPIRADA. Vuelve a calcular el adeudo.` Ya pagada u otro estado: `409 LIQUIDACION_NO_ACTIVA`.

Efectos del autorizado:

- predial: marca esos periodos `pagado=true`
- agua: marca el contrato `pagado=true`
- licencia: trámites pasa a `PAGADO`
- multa: `pagada=true`
- siempre: documento oficial y fila en `platform.bitacora` (`accion=PAGO_AUTORIZADO`)

Folio de pago: `PAG-` + año + `-` + secuencia de 6 dígitos.

## Trámites (HU-05, HU-06 y subsanación)

Forma de un trámite:

```json
{
  "folio": "TRM-2026-000001",
  "tipo": "LICENCIA_FUNCIONAMIENTO",
  "estado": "EN_REVISION",
  "titulo": "Licencia de funcionamiento Abarrotes Luna",
  "ciudadanoId": "uuid",
  "liquidacionId": null,
  "creadoEn": "2026-11-07T12:00:00Z",
  "payload": { "giro": "ABARROTES", "superficieM2": 40, "total": 1280.00, "nombreComercial": "Abarrotes Luna" },
  "lineaTiempo": [
    { "estado": "EN_REVISION", "nota": "Solicitud recibida para revisión técnica", "actor": "María López Hernández", "creadoEn": "2026-11-07T12:00:00Z" }
  ]
}
```

Folio: `TRM-` + año + `-` + secuencia de 6 dígitos. El folio no cambia en la subsanación.

`POST /api/v1/tramites/licencias-construccion` `201` estado `RADICADO`, `liquidacionId` null.

```json
{
  "tipoObra": "OBRA_MENOR",
  "claveCatastral": "05-021-0147-003",
  "descripcion": "Ampliación de losa de azotea",
  "documentos": [
    { "nombre": "plano.pdf", "contentType": "application/pdf", "contenidoBase64": "JVBERg==" }
  ]
}
```

Sin documentos: `400 ADJUNTO_OBLIGATORIO`, campo `documentos`, ejemplo `plano.pdf`. Clave inválida: el mismo error de catastro. Título: `Licencia de construcción <tipoObra>`.

`GET /api/v1/tramites/folio/{folio}` cualquier usuario del municipio autenticado. `404 TRAMITE_NO_ENCONTRADO`, campo `folio`, ejemplo `TRM-2026-000001`.

`GET /api/v1/tramites/mios` trámites del ciudadano del token.

`GET /api/v1/tramites/bandeja` rol `SERVIDOR_PUBLICO` o `ADMINISTRADOR`. Arreglo de trámites en `RADICADO`, `EN_REVISION` o `POR_SUBSANAR`.

`POST /api/v1/tramites/folio/{folio}/dictamen` rol servidor o admin.

```json
{ "decision": "OBSERVAR", "observaciones": "El plano no indica la escala." }
```

`APROBAR` en funcionamiento: estado `APROBADO_PENDIENTE_PAGO` y nace la liquidación. `APROBAR` en construcción: estado `APROBADO`. `OBSERVAR`: estado `POR_SUBSANAR`. Observación vacía al observar: `400 OBSERVACION_OBLIGATORIA`, campo `observaciones`, ejemplo `Falta el plano de fachada.` Cada dictamen agrega una entrada a `lineaTiempo` y una fila de bitácora.

`POST /api/v1/tramites/folio/{folio}/subsanar` solo el dueño y solo si el estado es `POR_SUBSANAR`.

```json
{ "documentos": [ { "nombre": "plano-v2.pdf", "contentType": "application/pdf", "contenidoBase64": "JVBERg==" } ] }
```

El folio se conserva. Funcionamiento vuelve a `EN_REVISION`. Construcción vuelve a `RADICADO`. Estado distinto: `409 ESTADO_NO_PERMITE`.

Cuando pagos confirma la licencia llama `POST /api/v1/tramites/interno/pagado` con `X-Internal-Token` y `{ "folio": "TRM-...", "liquidacionId": "uuid" }`. No está en el gateway. Pasa el trámite a `PAGADO` y agrega la línea de tiempo.

## Documentos (HU-07)

`GET /api/v1/documentos/{id}` metadatos, sin el binario:

```json
{
  "id": "uuid",
  "folio": "PAG-2026-000001",
  "tipo": "RECIBO_PAGO",
  "transaccionId": "uuid",
  "titular": "María López Hernández",
  "concepto": "Impuesto predial 2025-6, 2026-1",
  "monto": 3116.40,
  "sello": "hex-hmac-sha256",
  "qrPayload": "http://localhost:3000/verificar/PAG-2026-000001",
  "emitidoEn": "2026-11-07T12:05:00Z"
}
```

`GET /api/v1/documentos/{id}/pdf` responde `application/pdf`. El PDF contiene folio, id de transacción, concepto, monto, sello y un QR del `qrPayload`.

`GET /api/v1/documentos/verificar/{folio}` es público.

```json
{ "valido": true, "folio": "PAG-2026-000001", "concepto": "...", "monto": 3116.40, "titular": "...", "emitidoEn": "...", "sello": "..." }
```

Folio desconocido: `200` `{ "valido": false, "folio": "PAG-2026-999999" }`.

El sello es HMAC-SHA256 en hex de `folio|monto|concepto|titular` con el secreto `gld-sello-demo`. El QR codifica `qrPayload`.

## Foros (HU-19)

`GET /api/v1/ciudad/foros` arreglo de `{ id, titulo, cuerpo, autorNombre, creadoEn, respuestas }`.

`POST /api/v1/ciudad/foros` `{ "titulo": "...", "cuerpo": "..." }` `201` el foro.

`GET /api/v1/ciudad/foros/{id}` foro más `mensajes`: `{ id, autorNombre, cuerpo, esOficial, creadoEn }`.

`POST /api/v1/ciudad/foros/{id}/mensajes` `{ "cuerpo": "..." }` `201`. `esOficial=false`.

`POST /api/v1/ciudad/foros/{id}/respuesta-oficial` `{ "cuerpo": "..." }` solo servidor o admin. `201` con `esOficial=true`.

## Encuestas (HU-20)

`GET /api/v1/ciudad/encuestas`

```json
[
  {
    "id": "f2222222-2222-4222-8222-222222222222",
    "pregunta": "¿Qué obra debe priorizar el municipio este trimestre?",
    "cierraEn": "2026-12-07T00:00:00Z",
    "cerrada": false,
    "yaVote": false,
    "opciones": [
      { "id": "f3333333-3333-4333-8333-333333333331", "texto": "Bacheo", "votos": 0 }
    ]
  }
]
```

`POST /api/v1/ciudad/encuestas` servidor o admin. `{ "pregunta": "...", "opciones": ["Sí", "No"], "dias": 15 }` `201`. Al menos dos opciones.

`POST /api/v1/ciudad/encuestas/{id}/votar` `{ "opcionId": "uuid" }` `204`. Segundo voto del mismo usuario: `409 YA_VOTO`. Encuesta con `cierraEn` pasado: `409 ENCUESTA_CERRADA`. Escribe bitácora `VOTO`.

## Directorio (HU-21)

`GET /api/v1/ciudad/pymes?q=` `q` filtra por nombre, giro o colonia, sin distinguir mayúsculas. Vacío devuelve todas.

Objeto: `{ id, nombre, giro, descripcion, telefono, colonia }`.

`POST /api/v1/ciudad/pymes`

```json
{ "nombre": "Café Norte", "giro": "Alimentos", "descripcion": "Café de la colonia", "telefono": "5599990000", "colonia": "Norte" }
```

`201` el objeto. El dueño es el usuario del token.

## Chat (HU-22)

`GET /api/v1/ciudad/salas` arreglo `{ id, nombre, tema }`.

`POST /api/v1/ciudad/salas` `{ "nombre": "...", "tema": "..." }` `201`.

`GET /api/v1/ciudad/salas/{id}/mensajes` arreglo `{ id, autorNombre, texto, creadoEn }` en orden cronológico.

`POST /api/v1/ciudad/salas/{id}/mensajes` `{ "texto": "Buenos días" }` `201`.

Si el texto contiene una palabra de `ciudad.palabras_bloqueadas`, comparación sin mayúsculas y sin acentos: `400 LENGUAJE_NO_PERMITIDO`, detalle `El mensaje contiene lenguaje no permitido y no se publicó.`, campo `texto`, ejemplo `Buenos días, vecinos`.

## Alertas (HU-23)

`GET /api/v1/ciudad/alertas/preferencias`

```json
{ "telefono": "5512340001", "temas": ["SISMO", "SERVICIOS"] }
```

Si no hay fila: `{ "telefono": "", "temas": [] }`.

`PUT /api/v1/ciudad/alertas/preferencias` el mismo cuerpo, `200` el guardado. Temas admitidos: `SISMO`, `INUNDACION`, `SERVICIOS`, `SALUD`.

`POST /api/v1/ciudad/alertas/ensayo` servidor o admin.

```json
{ "tema": "SISMO", "mensaje": "Simulacro a las 11:00" }
```

`200`

```json
{ "tema": "SISMO", "destinatarios": 1 }
```

Inserta una fila en `alertas_enviadas` por cada preferencia del municipio cuyo arreglo contiene el tema. No llama a un proveedor SMS.

## Agenda (HU-25)

`GET /api/v1/ciudad/eventos` arreglo `{ id, titulo, descripcion, lugar, iniciaEn, inscrito }`. `inscrito` es del usuario del token.

`POST /api/v1/ciudad/eventos` servidor o admin.

```json
{ "titulo": "Jornada de salud", "descripcion": "Módulos de vacunación", "lugar": "Plaza principal", "iniciaEn": "2026-12-01T16:00:00Z" }
```

`201` el evento con `inscrito=false`.

`POST /api/v1/ciudad/eventos/{id}/inscripcion` `204`. Repetir también `204`.
`DELETE /api/v1/ciudad/eventos/{id}/inscripcion` `204`.

## Incidencias (HU-24)

`POST /api/v1/incidencias/vista-previa`

```json
{ "lat": 19.432600, "lng": -99.133200 }
```

`200`

```json
{ "direccionReferencia": "Referencia aproximada 19.432600, -99.133200, zona centro" }
```

La dirección es editable. El portal la manda de vuelta en el alta, ya corregida si el ciudadano la cambió.

`POST /api/v1/incidencias`

```json
{
  "tipo": "BACHE",
  "lat": 19.432600,
  "lng": -99.133200,
  "direccionReferencia": "Av. Reforma 120, frente al número 118",
  "descripcion": "Bache profundo en el carril derecho",
  "fotoBase64": null,
  "fotoContentType": null
}
```

`tipo`: `BACHE`, `ALUMBRADO`, `AGUA`, `BASURA`. `201`:

```json
{
  "folio": "INC-2026-000001",
  "tipo": "BACHE",
  "estado": "ASIGNADA",
  "direccionReferencia": "Av. Reforma 120, frente al número 118",
  "lat": 19.432600,
  "lng": -99.133200,
  "descripcion": "Bache profundo en el carril derecho",
  "brigada": { "nombre": "Brigada de bacheo", "zona": "Centro" },
  "creadaEn": "2026-11-07T12:10:00Z"
}
```

Folio `INC-` + año + `-` + 6 dígitos. La brigada es la del mismo `tipo` en el municipio. Escribe bitácora `INCIDENCIA_REPORTADA`. Latitud fuera de -90..90 o longitud fuera de -180..180: `400`, campo `lat` o `lng`, ejemplo `19.432600`.

`GET /api/v1/incidencias/folio/{folio}` el mismo objeto más `tieneFoto`.
`GET /api/v1/incidencias/mias` arreglo de esos objetos.
`GET /api/v1/incidencias/{id}/foto` el binario con su content type, o `404` si no hay foto.

## Salud

Cada proceso expone `GET /actuator/health`. El gateway también.

## Cuentas de la semilla

| Correo | Rol | Clave |
| --- | --- | --- |
| ciudadano@gld.demo | CIUDADANO | Demo123! |
| servidor@gld.demo | SERVIDOR_PUBLICO | Demo123! |
| admin@gld.demo | ADMINISTRADOR | Demo123! |

Tenant `05021`. Predio `05-021-0147-003`. Contrato `CTA-1001`. Placa `ABC-123`.
