# Diagrama de arquitectura

El portal solo habla con el gateway. Cada fila es un servicio y la base de la que es dueño. No hay llaves foráneas entre bases.

```mermaid
flowchart TB
  ciudadano((Ciudadano)) --> portal["Portal Next.js 3000"]
  servidor(("Servidor público")) --> portal
  portal --> gateway["gld-gateway 8080"]
  gateway --> servicios

  subgraph servicios [Microservicios y su base]
    direction TB
    identity["identity 8081"] --> dbIdentity[("gld_identity")]
    dbIdentity ~~~ catastro["catastro 8082"]
    catastro --> dbCatastro[("gld_catastro")]
    dbCatastro ~~~ pagos["pagos 8083"]
    pagos --> dbPagos[("gld_pagos")]
    dbPagos ~~~ tramites["tramites 8084"]
    tramites --> dbTramites[("gld_tramites")]
    dbTramites ~~~ documentos["documentos 8085"]
    documentos --> dbDocumentos[("gld_documentos")]
    dbDocumentos ~~~ ciudad["ciudad digital 8086"]
    ciudad --> dbCiudad[("gld_ciudad")]
    dbCiudad ~~~ incidencias["incidencias 8087"]
    incidencias --> dbIncidencias[("gld_incidencias")]
  end
```

Las llamadas entre servicios no pasan por el gateway. Van al puerto del servicio con el encabezado `X-Internal-Token`.

| Desde | Hacia | Para qué |
| --- | --- | --- |
| identity | ciudad digital | crear el perfil al registrarse |
| ciudad digital | identity | saber si el TOTP está activo |
| pagos | catastro | leer el predio o el contrato y marcarlo pagado |
| pagos | trámites | avisar que el trámite quedó pagado |
| pagos | documentos | emitir el recibo |
| trámites | pagos | cotizar y liquidar la licencia |
| trámites | catastro | validar la clave catastral |
