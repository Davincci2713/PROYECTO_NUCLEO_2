# Especificación Técnica y Requerimientos de Dominio para la Plataforma Gobiernos Locales Digitales (GLD)

La **Plataforma Gobiernos Locales Digitales (GLD)** es una solución integral de e-Gobierno orientada a modernizar la administración pública municipal. Este documento consolida la arquitectura de software, los requerimientos funcionales y no funcionales, el modelo de datos multi-tenant, las reglas de dominio, el backlog completo de historias de usuario y las directrices de interfaz derivadas de pruebas de usabilidad, estructurado para orientar la implementación técnica mediante una arquitectura de microservicios.

---

## 1. Contexto Estratégico y Paradigma de Arquitectura

El proyecto surge como una reingeniería del sistema original desarrollado por el CENETI (Centro Nacional de Estudios de Tecnología Informática) e Infotec. El diagnóstico del modelo original reveló una falla crítica en la adopción: en dos años únicamente se incorporaron 5 municipios, alcanzando tan solo un 26% del escenario pesimista proyectado de 19 municipios. 

Las causas fundamentales del fracaso operativo incluyeron:
- **Ciclos políticos municipales acotados (3 años)**: Las administraciones locales evitan inversiones elevadas en infraestructura o desarrollo de software a medida que no muestren resultados inmediatos.
- **Canales de distribución deficientes**: Dependencia de pequeñas PyMEs de TI locales sin experiencia en contrataciones gubernamentales (B2G).
- **Altos costos y tiempos de despliegue**: Dificultades para instalar y mantener instancias aisladas en servidores físicos locales.

Para superar estas barreras, la reingeniería desarrollada establece una **plataforma SaaS Multi-Tenant** basada en una arquitectura de microservicios sobre contenedores. La estrategia adopta un modelo de aislamiento de datos **schema-per-tenant**, garantizando privacidad y personalización por municipio, con un objetivo de aprovisionamiento de **6 semanas** por cliente y un modelo financiero de renta de **\$25,000 MXN/mes**, eliminando gastos de arranque.

---

## 2. Requerimientos No Funcionales (RNF) — ISO/IEC 25010

Los 14 Requerimientos No Funcionales definidos para el sistema establecen los criterios de calidad operativa y técnica que deben ser satisfechos por la arquitectura de microservicios:

| ID RNF | Atributo de Calidad | Especificación y Umbral Cuantitativo Medible |
| :--- | :--- | :--- |
| **RNF-01** | Eficiencia de Rendimiento | Tiempo de respuesta en consultas de adeudo $< 2$ segundos para el 95% de peticiones con 500 usuarios concurrentes. |
| **RNF-02** | Eficiencia de Rendimiento | Tiempo de procesamiento completo de transacciones de pago $< 5$ segundos tras confirmación bancaria. |
| **RNF-03** | Disponibilidad | Disponibilidad operativa continua del **99.0%** en infraestructura Data Center Tier II. |
| **RNF-04** | Escalabilidad | Capacidad para atender al menos **50 municipios** activos simultáneos dentro de la misma instancia de plataforma. |
| **RNF-05** | Seguridad / Autenticación | Control de acceso centralizado basado en **OAuth 2.0 / OpenID Connect (OIDC)** con Autenticación de Dos Factores (2FA). |
| **RNF-06** | Seguridad / Cifrado | Cifrado obligatorio en tránsito mediante **TLS 1.3** y cifrado en reposo de datos sensibles mediante **AES-256**. |
| **RNF-07** | Seguridad / Transaccional | Cumplimiento **PCI-DSS** delegado íntegramente a la pasarela bancaria externa sin almacenamiento de tarjetas en el sistema. |
| **RNF-08** | Usabilidad / Accesibilidad | Flujos de trámites acotados a un **máximo de 5 pasos** con cumplimiento de pautas **WCAG 2.1 nivel AA**. |
| **RNF-09** | Usabilidad / Adaptabilidad | Interfaz web totalmente adaptable (*responsive*) funcional desde pantallas con ancho mínimo de **360 píxeles**. |
| **RNF-10** | Mantenibilidad | Cobertura de pruebas automatizadas (unitarias y de integración) $\ge 80\%$. |
| **RNF-11** | Portabilidad | Arquitectura desacoplada de nube (*cloud-agnostic*), sin dependencias propietarias de un único proveedor. |
| **RNF-12** | Interoperabilidad | Documentación y exposición de contratos de interfaz bajo el estándar **OpenAPI 3.0**. |
| **RNF-13** | Resiliencia / DR | Objetivos de recuperación operativa ante desastres: **RPO $\le 1$ hora** y **RTO $\le 4$ horas**. |
| **RNF-14** | Auditabilidad | Retención inalterable de bitácoras de auditoría transaccional y financiera por un mínimo de **5 años**. |

---

## 3. Arquitectura del Sistema y Mapeo por Bounded Contexts

La plataforma se compone de 11 microservicios especializados organizados por contextos acotados (*Bounded Contexts*), estructurados en seis capas arquitectónicas:

### 3.1 Estructura por Capas
1. **Capa de Presentación**: Aplicaciones web desarrolladas en React / Next.js bajo pautas WCAG 2.1 AA, divididas en el Portal Ciudadano (Ventanilla Única y Ciudad Digital) y el BackOffice del Servidor Público.
2. **Capa de Borde (API Gateway & IAM)**: Kong API Gateway encargado del enrutamiento, tasa límite (*rate limiting*) e inyección del identificador de inquilino (`X-Tenant-ID`); Keycloak como proveedor de identidad IAM.
3. **Capa de Aplicación (Microservicios)**: Servicios desacoplados desarrollados en Java 21 con Spring Boot 3.
4. **Capa de Eventos y Mensajería**: Apache Kafka para la gestión de eventos asíncronos y transacciones distribuidas bajo el patrón SAGA coreografiado.
5. **Capa de Persistencia**: PostgreSQL 16 con extensión PostGIS (aislamiento *schema-per-tenant*), Redis para caché de sesiones y catálogos, MinIO para expedientes/documentos PDF, y Metabase para tableros de indicadores.
6. **Capa de Despliegue e Integraciones**: Infraestructura orquestada con Kubernetes, Docker y Terraform; integraciones externas con pasarelas multibanco (PSE / Tarjetas), Proveedor de Servicios de Certificación (PSC para firma digital) y pasarela SMS.

---

## 4. Catálogo Detallado de Microservicios

### MS-01: API Gateway y Servicio de Identidad (`Kong` / `Keycloak`)
* **Propósito**: Gestionar el punto único de entrada, enrutamiento dinámico por subdominio (`municipio.gld.gob.mx`), autenticación OIDC con 2FA y autorización basada en roles (Ciudadano, Servidor Público, Tesorero, Administrador).
* **Mecanismo Multi-tenant**: Extrae el identificador del inquilino desde el encabezado HTTP o subdominio y lo propaga a los servicios internos mediante el encabezado `X-Tenant-ID`.

### MS-02: Microservicio de Pagos y Liquidación (`MS-Pagos`)
* **Propósito**: Realizar el cálculo de liquidaciones tributarias (predial, agua, licencias, multas), aplicar recargos por mora o descuentos (2% predial en línea, 50% pronto pago de multas) y procesar transacciones seguras con la pasarela multibanco.
* **Persistencia**: Tablas `liquidaciones` y `transacciones_pago`.
* **Eventos Producidos**: `pago.autorizado.v1`, `pago.rechazado.v1`.

### MS-03: Microservicio de Trámites y Expedientes (`MS-Tramites`)
* **Propósito**: Administrar la radicación digital, asignación de folios únicos de rastreo, actualización de la línea de tiempo del expediente, gestión de archivos adjuntos y control del temporizador de 72 horas para liquidaciones impagas.
* **Persistencia**: Tablas `tramites`, `expedientes`, `historial_estados`.
* **Eventos Consumidos**: `pago.autorizado.v1`.
* **Eventos Producidos**: `tramite.radicado.v1`, `tramite.aprobado.v1`, `tramite.subsanado.v1`.

### MS-04: Microservicio de Documentación y Firma Digital (`MS-Documentos`)
* **Propósito**: Generar comprobantes digitales oficiales en PDF, aplicar el sello electrónico encriptado de la Tesorería Municipal, renderizar códigos QR de autenticidad (vía librería QRious) y almacenar los archivos en MinIO.
* **Persistencia**: Tablas `documentos_oficiales`, `sellos_digitales`.

### MS-05: Microservicio de Contabilidad Gubernamental y ERP (`MS-Contabilidad`)
* **Propósito**: Ejecutar la contabilidad armonizada, registrando automáticamente los asientos contables en sus momentos (Devengado / Recaudado) al aprobar un pago, controlando partidas presupuestales de ingresos y egresos, y ejecutando conciliaciones bancarias.
* **Persistencia**: Tablas `asientos_contables`, `partidas_presupuestales`, `cuentas_bancarias`.
* **Eventos Consumidos**: `pago.autorizado.v1`.

### MS-06: Microservicio de Adquisiciones, Licitaciones y Almacén (`MS-Adquisiciones`)
* **Propósito**: Gestionar requisiciones de insumos, licitaciones públicas, emisión de órdenes de compra vinculadas al presupuesto y control de entradas/salidas en el almacén municipal.
* **Persistencia**: Tablas `requisiciones`, `licitaciones`, `ordenes_compra`, `inventario_almacen`.

### MS-07: Microservicio de Recursos Humanos y Nómina (`MS-RRHH`)
* **Propósito**: Administrar el catálogo de puestos y organigrama municipal, expedientes digitales de empleados, cálculo de retenciones impositivas, dispersión bancaria masiva y emisión de recibos de nómina.
* **Persistencia**: Tablas `empleados`, `organigrama_plazas`, `recibos_nomina`.

### MS-08: Microservicio de Padrón Catastral y Georreferenciación (`MS-Catastro`)
* **Propósito**: Mantener la base gravable municipal, registro de predios, cálculo de avalúos según valor de suelo/construcción y gestión de capas cartográficas en PostGIS.
* **Persistencia**: PostgreSQL 16 con PostGIS (`predios_catastrales`, `capas_gis`).

### MS-09: Microservicio de Servicios Públicos e Incidencias (`MS-Incidencias`)
* **Propósito**: Procesar reportes ciudadanos de fallas urbanas (baches, alumbrado, agua, basura) con foto y ubicación en mapa, derivando automáticamente coordenadas a direcciones de referencia y asignando órdenes a brigadas operativas.
* **Persistencia**: PostgreSQL 16 con PostGIS (`incidencias_urbanas`, `brigadas_operativas`).

### MS-10: Microservicio de Ciudad Digital y Red Social (`MS-CiudadDigital`)
* **Propósito**: Gestionar perfiles vecinales, foros con respuesta oficial, encuestas ciudadanas con restricción de un voto por cuenta, directorio PyME local, salas de chat temáticas moderadas, suscripciones a alertas SMS y agenda de eventos.
* **Persistencia**: Tablas `perfiles_ciudadanos`, `foros_temas`, `encuestas_votos`, `directorio_pymes`, `alertas_sms`.

### MS-11: Microservicio de Transparencia e Interoperabilidad (`MS-Transparencia-Interop`)
* **Propósito**: Publicar formatos normativos de transparencia, buscador indexado de acuerdos/reglamentos en PDF, atención a solicitudes de información pública (SIPA local) y exposición/consumo de servicios web (OpenAPI 3.0) para interoperabilidad con instancias estatales y federales.
* **Persistencia**: Tablas `obligaciones_transparencia`, `solicitudes_sipa`, `documentos_publicos`.

---

## 5. Backlog Completo de Historias de Usuario (30 HU / 280 Story Points)

El desarrollo del sistema está respaldado por 30 historias de usuario especificadas bajo el formato de Cohn y estimadas mediante Planning Poker (280 Story Points en total), distribuidas en cuatro épicas:

```
Volumen Total del Backlog: 30 HU | 280 Story Points (SP)
├── Épica 1: Ventanilla Única y Trámites (8 HU | 73 SP)
├── Épica 2: BackOffice y ERP Municipal (9 HU | 105 SP)
├── Épica 3: Ciudad Digital y Red Social Cívica (8 HU | 57 SP)
└── Épica 4: Transparencia e Interoperabilidad (5 HU | 45 SP)
```

### 5.1 Épica 1: Ventanilla Única y Trámites (8 HU | 73 SP)

#### HU-01: Pago Electrónico de Impuesto Predial
* **Prioridad**: Must Have | **Estimación**: 13 SP | **Microservicio**: `MS-Pagos` / `MS-Catastro`
* **Historia**: Como ciudadano, quiero consultar y pagar en línea mi impuesto predial, para evitar desplazamientos a la tesorería municipal.
* **Criterios de Aceptación (Dado-Cuando-Entonces)**:
  - *Dado* un predio con periodos pendientes consultado por clave catastral, *cuando* el ciudadano selecciona los bimestres a pagar, *entonces* el sistema calcula el impuesto, los recargos por mora y aplica un 2% de descuento por pago en línea sobre el subtotal.
  - *Dado* un pago autorizado por la pasarela bancaria, *cuando* se recibe la confirmación, *entonces* se genera un folio único, se emite el recibo oficial con sello y código QR, y se actualiza el estado del adeudo.
  - *Dado* un rechazo en la pasarela bancaria, *cuando* se informa al ciudadano, *entonces* la liquidación se conserva activa y se ofrece reintentar con otro medio de pago.

#### HU-02: Pago de Derechos de Agua Potable y Alcantarillado
* **Prioridad**: Must Have | **Estimación**: 13 SP | **Microservicio**: `MS-Pagos`
* **Historia**: Como ciudadano, quiero consultar el estado de cuenta y pagar el servicio de agua, para mantener mi suministro al día.
* **Criterios de Aceptación**:
  - *Dado* un número de contrato o medidor, *cuando* el usuario consulta su saldo, *entonces* se desglosa el consumo en $m^3$, las lecturas del periodo y el importe total.
  - *Dado* el pago procesado, *cuando* se confirma la transacción, *entonces* el módulo de tesorería del BackOffice actualiza el saldo de forma inmediata.

#### HU-03: Expedición y Pago de Licencias de Funcionamiento Comercial
* **Prioridad**: Must Have | **Estimación**: 8 SP | **Microservicio**: `MS-Pagos` / `MS-Tramites`
* **Historia**: Como comerciante, quiero tramitar y pagar mi licencia de negocio en línea, para iniciar operaciones rápidamente.
* **Criterios de Aceptación**:
  - *Dado* la captura del giro comercial y la superficie del local, *cuando* se realiza la solicitud, *entonces* el sistema calcula los derechos de expedición automáticamente.
  - *Dado* la aprobación técnica en BackOffice, *cuando* se confirma el pago, *entonces* se emite el documento oficial con código QR de verificación.

#### HU-04: Consulta y Pago de Multas de Tránsito y Sanciones
* **Prioridad**: Must Have | **Estimación**: 8 SP | **Microservicio**: `MS-Pagos`
* **Historia**: Como ciudadano, quiero consultar e impugnar/pagar infracciones de tránsito en el portal, para regularizar mi situación vehicular.
* **Criterios de Aceptación**:
  - *Dado* una placa o folio de infracción, *cuando* se consulta el sistema, *entonces* se listan las multas vigentes.
  - *Dado* una infracción dentro del plazo de pronto pago (primeros 10 días), *cuando* se procede al cobro, *entonces* el sistema aplica automáticamente el 50% de descuento.

#### HU-05: Licencias de Construcción y Permisos de Uso de Suelo
* **Prioridad**: Must Have | **Estimación**: 13 SP | **Microservicio**: `MS-Tramites` / `MS-Documentos`
* **Historia**: Como constructor o ciudadano, quiero solicitar licencias de obra menor y uso de suelo, para cumplir la normativa urbana.
* **Criterios de Aceptación**:
  - *Dado* una solicitud de construcción, *cuando* el ciudadano adjunta los planos y requisitos requeridos, *entonces* el sistema radicará el expediente sin cobro en pasarela y lo derivará a revisión técnica en Desarrollo Urbano.

#### HU-06: Radicación Digital y Seguimiento por Folio de Trámites
* **Prioridad**: Should Have | **Estimación**: 5 SP | **Microservicio**: `MS-Tramites`
* **Historia**: Como ciudadano, quiero consultar el estado de mi solicitud con un folio único, para conocer el avance de mi trámite.
* **Criterios de Aceptación**:
  - *Dado* un folio de trámite generado, *cuando* el ciudadano consulta la sección de seguimiento, *entonces* se presenta una línea de tiempo visual con el estado del proceso.
  - *Dado* un cambio de estado en la bandeja del servidor público, *cuando* este se registra, *entonces* la línea de tiempo del ciudadano se actualiza al instante.

#### HU-07: Emisión de Recibos Digitales Oficiales y Sello Electrónico
* **Prioridad**: Must Have | **Estimación**: 5 SP | **Microservicio**: `MS-Documentos`
* **Historia**: Como contribuyente, quiero descargar mi comprobante oficial de pago, para efectos de validez fiscal y administrativa.
* **Criterios de Aceptación**:
  - *Dado* un pago confirmado, *cuando* se emite el comprobante en PDF, *entonces* este contiene folio único, ID de transacción, sello digital firmado por Tesorería y código QR para verificación externa.

#### HU-08: Integración de Pasarela de Pagos Multibanco e Infraestructura Tier II
* **Prioridad**: Must Have | **Estimación**: 8 SP | **Microservicio**: `MS-Pagos` / Gateway
* **Historia**: Como administrador del portal, quiero habilitar múltiples medios de pago seguros, para maximizar la recaudación municipal.
* **Criterios de Aceptación**:
  - *Dado* la selección de pago, *cuando* el usuario decide pagar por Tarjeta de Crédito/Débito o PSE, *entonces* la pasarela muestra de manera transparente el concepto e importe a cobrar antes de capturar datos financieros.

---

### 5.2 Épica 2: BackOffice y Administración / ERP Municipal (9 HU | 105 SP)

#### HU-09: Gestión de Contabilidad Gubernamental e Ingresos
* **Prioridad**: Must Have | **Estimación**: 13 SP | **Microservicio**: `MS-Contabilidad`
* **Historia**: Como tesorero municipal, quiero registrar automáticamente los ingresos del portal y controlar el presupuesto, para cumplir la ley contable.
* **Criterios de Aceptación**:
  - *Dado* un trámite pagado en la bandeja del servidor público, *cuando* el funcionario aprueba el expediente, *entonces* el sistema registra de inmediato el asiento contable del ingreso afectando las partidas correspondientes (Devengado / Recaudado).

#### HU-10: Gestión de Egresos, Conciliación Bancaria y Cierre Contable
* **Prioridad**: Must Have | **Estimación**: 13 SP | **Microservicio**: `MS-Contabilidad`
* **Historia**: Como contador municipal, quiero conciliar cuentas y autorizar egresos presupuestales, para asegurar la transparencia financiera.
* **Criterios de Aceptación**:
  - *Dado* la carga de un extracto bancario, *cuando* se ejecuta la conciliación, *entonces* el sistema enlaza automáticamente los movimientos con los registros contables e identifica discrepancias.

#### HU-11: Administración de Requisiciones, Licitaciones y Compras
* **Prioridad**: Must Have | **Estimación**: 8 SP | **Microservicio**: `MS-Adquisiciones`
* **Historia**: Como encargado de compras, quiero gestionar requisiciones y licitaciones digitalmente, para optimizar el gasto de insumos.

#### HU-12: Control de Inventarios, Entradas/Salidas y Almacén Municipal
* **Prioridad**: Must Have | **Estimación**: 8 SP | **Microservicio**: `MS-Adquisiciones`
* **Historia**: Como jefe de almacén, quiero registrar las entradas y salidas de materiales, para mantener existencias actualizadas.

#### HU-13: Administración de Recursos Humanos y Expediente de Personal
* **Prioridad**: Must Have | **Estimación**: 8 SP | **Microservicio**: `MS-RRHH`
* **Historia**: Como director de RRHH, quiero administrar la estructura organizacional y expedientes, para controlar la plantilla laboral.

#### HU-14: Cálculo, Dispersión Electrónica y Recibos de Nómina
* **Prioridad**: Must Have | **Estimación**: 13 SP | **Microservicio**: `MS-RRHH`
* **Historia**: Como analista de nómina, quiero calcular la nómina municipal y generar comprobantes, para dispersar sueldos a tiempo.

#### HU-15: Control Patrimonial de Activos Fijos y Vehículos Municipales
* **Prioridad**: Should Have | **Estimación**: 8 SP | **Microservicio**: `MS-Contabilidad` / ERP
* **Historia**: Como administrador de bienes, quiero registrar el parque vehicular y activos fijos, para garantizar la conservación del patrimonio.

#### HU-16: Registro y Control de Padrón Catastral Municipal
* **Prioridad**: Must Have | **Estimación**: 21 SP | **Microservicio**: `MS-Catastro`
* **Historia**: Como director de catastro, quiero administrar el inventario predial y avalúos, para mantener actualizada la base gravable.
* **Criterios de Aceptación**:
  - *Dado* la modificación de un predio o zona fiscal, *cuando* se guarda el cambio en la capa espacial, *entonces* los valores impositivos se actualizan y sincronizan automáticamente con la Ventanilla Única.

#### HU-17: Generación de Reportes Financieros y Cuentas Públicas
* **Prioridad**: Must Have | **Estimación**: 13 SP | **Microservicio**: `MS-Contabilidad`
* **Historia**: Como auditor interno, quiero emitir estados financieros normativos, para cumplir con los órganos de fiscalización estatal.

---

### 5.3 Épica 3: Ciudad Digital y Red Social Cívica (8 HU | 57 SP)

#### HU-18: Perfiles Ciudadanos, Red Social y Autenticación
* **Prioridad**: Could Have | **Estimación**: 8 SP | **Microservicio**: `MS-CiudadDigital`
* **Historia**: Como ciudadano, quiero registrarme en la ciudad digital con verificación y 2FA, para interactuar con la comunidad y servicios.

#### HU-19: Creación de Blogs, Wikis y Foros de Discusión Comunitaria
* **Prioridad**: Could Have | **Estimación**: 5 SP | **Microservicio**: `MS-CiudadDigital`
* **Historia**: Como vecino, quiero publicar en foros locales y recibir respuestas oficiales etiquetadas del municipio.

#### HU-20: Módulo de Encuestas, Votaciones y Consultas Ciudadanas
* **Prioridad**: Could Have | **Estimación**: 5 SP | **Microservicio**: `MS-CiudadDigital`
* **Historia**: Como administrador social, quiero publicar encuestas restrictas a un voto por ciudadano verificado, para medir la opinión de la población.

#### HU-21: Directorio Comercial y Promoción de PYMES Locales
* **Prioridad**: Should Have | **Estimación**: 8 SP | **Microservicio**: `MS-CiudadDigital`
* **Historia**: Como comerciante local, quiero dar de alta mi PyME en el directorio del portal, para impulsar mis ventas en la región.

#### HU-22: Salas de Chat Temáticas y Grupos de Interés Local
* **Prioridad**: Could Have | **Estimación**: 5 SP | **Microservicio**: `MS-CiudadDigital`
* **Historia**: Como ciudadano, quiero ingresar a salas de chat temáticas con moderación automática de lenguaje, para comunicarme con vecinos.

#### HU-23: Sistema de Alertas por SMS y Mensajería de Emergencias
* **Prioridad**: Should Have | **Estimación**: 8 SP | **Microservicio**: `MS-CiudadDigital`
* **Historia**: Como ciudadano, quiero recibir alertas institucionales por SMS según temas de preferencia, para prevenirme ante emergencias.

#### HU-24: Reporte Ciudadano de Servicios Públicos con Geolocalización
* **Prioridad**: Should Have | **Estimación**: 13 SP | **Microservicio**: `MS-Incidencias`
* **Historia**: Como ciudadano, quiero reportar fallas en baches, alumbrado o basura marcando el punto en el mapa y adjuntando foto, para solicitar atención.
* **Criterios de Aceptación**:
  - *Dado* la selección del punto en el mapa espacial, *cuando* el ciudadano marca la falla, *entonces* el sistema convierte las coordenadas en una dirección de referencia editable y genera un folio de atención asignado a la brigada responsable.

#### HU-25: Calendario Interactivo de Eventos y Agenda de Alcaldía
* **Prioridad**: Should Have | **Estimación**: 5 SP | **Microservicio**: `MS-CiudadDigital`
* **Historia**: Como ciudadano, quiero consultar la agenda pública del alcalde y eventos comunitarios, para participar e inscribirme.

---

### 5.4 Épica 4: Transparencia e Interoperabilidad (5 HU | 45 SP)

#### HU-26: Portal de Obligaciones de Transparencia y Licitaciones
* **Prioridad**: Must Have | **Estimación**: 8 SP | **Microservicio**: `MS-Transparencia-Interop`
* **Historia**: Como ciudadano, quiero revisar los contratos, sueldos y licitaciones del gobierno, para evaluar la rendición de cuentas.

#### HU-27: Buscador Avanzado de Documentos Públicos y Reglamentos
* **Prioridad**: Should Have | **Estimación**: 5 SP | **Microservicio**: `MS-Transparencia-Interop`
* **Historia**: Como estudiante o investigador, quiero buscar acuerdos de cabildo y reglamentos en PDF, para consultar la normatividad local.

#### HU-28: Sistema de Solicitudes de Información Pública (SIPA Local)
* **Prioridad**: Must Have | **Estimación**: 8 SP | **Microservicio**: `MS-Transparencia-Interop`
* **Historia**: Como ciudadano, quiero radicar solicitudes de acceso a la información pública, para ejercer mi derecho a la transparencia.

#### HU-29: Módulo de Difusión de Noticias, Fotografías y Comunicados
* **Prioridad**: Should Have | **Estimación**: 3 SP | **Microservicio**: `MS-Transparencia-Interop`
* **Historia**: Como comunicador municipal, quiero publicar boletines y galerías institucionales, para informar sobre obras realizadas.

#### HU-30: Plataforma de Interoperabilidad e-Gob (Estado/Federación)
* **Prioridad**: Must Have | **Estimación**: 21 SP | **Microservicio**: `MS-Transparencia-Interop`
* **Historia**: Como arquitecto de TI, quiero intercambiar datos con sistemas de gobierno estatal y federal mediante APIs REST seguras, para automatizar la gestión multinivel.

---

## 6. Reglas de Negocio Complejas y Operación Transaccional

### 6.1 Regla del Temporizador de 72 Horas (BPMN / RF-07)
En todo trámite que implique una liquidación financiera generada en línea, la plataforma debe iniciar un temporizador asíncrono de **72 horas**. Si al finalizar dicho lapso la pasarela de pago o la caja municipal no registran la confirmación del cobro:
1. La liquidación de adeudo pasa automáticamente a estado `EXPIRADA`.
2. Se cancela la reserva de transacción en `MS-Pagos`.
3. Si el ciudadano intenta realizar el pago posteriormente, el sistema recalculará los recargos e impuestos a la fecha actual antes de permitir el cobro.

### 6.2 Flujo de Subsanación de Expedientes
Cuando un expediente en revisión por parte de un funcionario público es rechazado por falta de documentos o inconsistencias técnicas:
1. El funcionario registra observaciones obligatorias en la bandeja de BackOffice.
2. El estado del expediente cambia a `POR_SUBSANAR`.
3. El ciudadano recibe una notificación por correo/SMS y visualiza las observaciones en su línea de tiempo, habilitándose la carga de nuevos documentos sin modificar el folio de radicación asignado originalmente.

---

## 7. Requerimientos de Interfaz y Usabilidad Derivados de Pruebas de Campo

La validación del prototipo funcional v1.0 mediante pruebas de usabilidad con usuarios externos identificó 6 hallazgos críticos (H-01 a H-06). Las soluciones aplicadas en la versión v2.0 constituyen reglas de diseño UI/UX obligatorias para los microservicios frontend:

1. **H-01 (Transparencia de Importe en Pago)**: El formulario de cobro debe exhibir en todo momento un encabezado prominente con el importe total exacto a cobrar y el concepto del trámite.
2. **H-02 (Navegación Segura de Regreso)**: Todos los formularios multi-paso deben incluir un enlace explícito de retroceso ("Volver al paso anterior") que preserve las opciones seleccionadas sin borrar el formulario.
3. **H-03 (Indicador de Pasos Visible)**: Incorporación de una barra de progreso numerada permanente (ej. *1. Consulta $\rightarrow$ 2. Adeudo $\rightarrow$ 3. Pago $\rightarrow$ 4. Recibo*) visible durante todo el recorrido del trámite.
4. **H-04 (Mensajes de Error Formateados)**: Reemplazar textos genéricos ("Datos inválidos") por mensajes que nombren el campo afectado y muestren un ejemplo de formato válido (ej. *Clave catastral incorrecta. Formato esperado: 05-021-0147-003*).
5. **H-05 (Guardado Rápido de Folios)**: Destacar el folio asignado en pantallas de confirmación y recibos, añadiendo un botón interactivo **"Copiar folio"** para evitar la transcripción manual.
6. **H-06 (Precarga de Dirección desde Mapa)**: Al hacer clic sobre el mapa para reportar una incidencia, el sistema debe derivar automáticamente las coordenadas a una dirección urbana aproximada e insertarla en el campo de referencia como texto editable.

---

## 8. Matriz de Trazabilidad y Inconsistencias Resueltas

Para garantizar la coherencia en la interpretación autónoma por parte de agentes de desarrollo, se resumen los emparejamientos y correcciones de trazabilidad identificados en el análisis técnico del proyecto:

### 8.1 Trazabilidad Requerimiento Funcional (RF) vs. Historia de Usuario (HU)
- **RF-01 & RF-02 (Predial y Pagos)** $\rightarrow$ Mapeados a `HU-01`, `HU-02`, `HU-03`, `HU-04` y `HU-08`.
- **RF-03 & RF-04 (Folios y Adjuntos)** $\rightarrow$ Mapeados a `HU-05` y `HU-06`.
- **RF-05 (Recibos con Firma/QR)** $\rightarrow$ Mapeado a `HU-07`.
- **RF-08 & RF-09 (Asientos Contables y Presupuesto)** $\rightarrow$ Mapeados a `HU-09` y `HU-10`.
- **RF-10, RF-11, RF-12 (Compras, Almacén, Nómina)** $\rightarrow$ Mapeados a `HU-11`, `HU-12`, `HU-13` y `HU-14`.
- **RF-13 (Catastro)** $\rightarrow$ Mapeado a `HU-16`.
- **RF-18 (Incidencias Geolocalizadas)** $\rightarrow$ Mapeado a `HU-24`.
- **RF-21 & RF-22 (Transparencia e Interoperabilidad)** $\rightarrow$ Mapeados a `HU-26` y `HU-30`.

### 8.2 Cobertura de Requerimientos sin HU Explícita en Backlog Original
- **RF-07 (Temporizador 72h)**: Implementado como regla de negocio asíncrona dentro del servicio `MS-Tramites`.
- **RF-14 (Bandeja por Área)**: Incorporado funcionalmente en el módulo de recepción y aprobación de trámites de `MS-Contabilidad` / BackOffice.
- **RF-23 (Tableros de Indicadores)**: Resuelto mediante el despliegue del componente analítico Metabase con acceso a las vistas contables y tributarias.
- **RF-24 (Bitácora inalterable 5 años)**: Garantizado mediante el patrón *Outbox* e ingesta de logs transaccionales en almacenamiento persistente con políticas de retención RNF-14.
