# Diagrama de clases

Modelo de dominio. El controlador devuelve un DTO y no aparece aquí. La flecha punteada es una llamada HTTP interna.

```mermaid
classDiagram
  direction TB

  class AuthService
  class Usuario {
    correo
    rol
    totpActivo
  }
  class CiudadService
  class CatastroService
  class PagosService
  class TramitesService
  class DocumentoService
  class IncidenciaService

  class Predio {
    claveCatastral
    propietario
  }
  class PeriodoPredial {
    periodo
    importeBase
    pagado
  }
  class ContratoAgua {
    numeroContrato
    pagado
  }
  class Liquidacion {
    tipo
    total
    estado
  }
  class Transaccion {
    medio
    estado
    monto
  }
  class Multa {
    folio
    placa
    pagada
  }
  class TarifaLicencia {
    giro
    tarifaBase
  }
  class Tramite {
    folio
    tipo
    estado
  }
  class Historial
  class Adjunto
  class DocumentoOficial {
    folio
    sello
    monto
  }
  class Foro
  class MensajeForo
  class Encuesta
  class Voto
  class Evento
  class Inscripcion
  class Pyme
  class Sala
  class MensajeChat
  class Incidencia {
    folio
    lat
    lng
  }
  class Brigada {
    tipo
    zona
  }

  AuthService --> Usuario
  CiudadService ..> AuthService : consulta TOTP
  PagosService ..> CatastroService : lee y marca pagado
  PagosService ..> DocumentoService : emite recibo
  TramitesService ..> PagosService : cotiza y liquida
  TramitesService ..> CatastroService : valida predio

  CatastroService --> Predio
  Predio "1" --> "*" PeriodoPredial
  CatastroService --> ContratoAgua
  PagosService --> Liquidacion
  Liquidacion "1" --> "*" Transaccion
  PagosService --> Multa
  PagosService --> TarifaLicencia
  TramitesService --> Tramite
  Tramite "1" --> "*" Historial
  Tramite "1" --> "*" Adjunto
  DocumentoService --> DocumentoOficial
  CiudadService --> Foro
  Foro "1" --> "*" MensajeForo
  CiudadService --> Encuesta
  Encuesta "1" --> "*" Voto
  CiudadService --> Evento
  Evento "1" --> "*" Inscripcion
  CiudadService --> Pyme
  CiudadService --> Sala
  Sala "1" --> "*" MensajeChat
  IncidenciaService --> Incidencia
  Brigada "1" --> "*" Incidencia
  Incidencia ..> Usuario : ciudadano
```

Preferencia de alerta y alerta enviada viven en ciudad digital, con el mismo corte servicio-entidad, y están en el esquema de `gld_ciudad`.
