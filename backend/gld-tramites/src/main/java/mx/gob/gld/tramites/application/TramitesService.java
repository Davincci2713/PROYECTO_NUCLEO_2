package mx.gob.gld.tramites.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mx.gob.gld.common.bitacora.BitacoraService;
import mx.gob.gld.common.error.Errores;
import mx.gob.gld.common.money.Secuencias;
import mx.gob.gld.common.security.Acceso;
import mx.gob.gld.common.security.GldPrincipal;
import mx.gob.gld.common.security.TenantContext;
import mx.gob.gld.common.web.ClaveCatastral;
import mx.gob.gld.tramites.infrastructure.AdjuntoEntity;
import mx.gob.gld.tramites.infrastructure.AdjuntoRepository;
import mx.gob.gld.tramites.infrastructure.HistorialEntity;
import mx.gob.gld.tramites.infrastructure.HistorialRepository;
import mx.gob.gld.tramites.infrastructure.TramiteEntity;
import mx.gob.gld.tramites.infrastructure.TramiteRepository;
import mx.gob.gld.tramites.web.TramitesDtos.DictamenRequest;
import mx.gob.gld.tramites.web.TramitesDtos.DocumentoAdjunto;
import mx.gob.gld.tramites.web.TramitesDtos.EventoLinea;
import mx.gob.gld.tramites.web.TramitesDtos.LicenciaConstruccionRequest;
import mx.gob.gld.tramites.web.TramitesDtos.LicenciaFuncionamientoRequest;
import mx.gob.gld.tramites.web.TramitesDtos.PagadoRequest;
import mx.gob.gld.tramites.web.TramitesDtos.SubsaniarRequest;
import mx.gob.gld.tramites.web.TramitesDtos.TramiteResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class TramitesService {

    private static final Set<String> DICTAMINABLES = Set.of("RADICADO", "EN_REVISION", "POR_SUBSANAR");
    private static final Set<String> BANDEJA = Set.of("RADICADO", "EN_REVISION", "POR_SUBSANAR");

    private final TramiteRepository tramites;
    private final HistorialRepository historial;
    private final AdjuntoRepository adjuntos;
    private final PagosTramiteCliente pagos;
    private final Secuencias secuencias;
    private final BitacoraService bitacora;
    private final ObjectMapper json;
    private final Clock clock;

    public TramitesService(TramiteRepository tramites,
                           HistorialRepository historial,
                           AdjuntoRepository adjuntos,
                           PagosTramiteCliente pagos,
                           Secuencias secuencias,
                           BitacoraService bitacora,
                           ObjectMapper json,
                           Clock clock) {
        this.tramites = tramites;
        this.historial = historial;
        this.adjuntos = adjuntos;
        this.pagos = pagos;
        this.secuencias = secuencias;
        this.bitacora = bitacora;
        this.json = json;
        this.clock = clock;
    }

    @Transactional
    public TramiteResponse licenciaFuncionamiento(LicenciaFuncionamientoRequest request) {
        if (request == null) {
            throw cuerpoVacio();
        }
        String nombre = exigir(request.nombreComercial(), "nombreComercial", "Abarrotes Luna", 160);
        if (request.giro() == null || request.giro().isBlank()) {
            throw Errores.giroDesconocido();
        }
        PagosTramiteCliente.Cotizacion cotizacion = pagos.cotizar(request.giro().trim(), request.superficieM2());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("giro", cotizacion.giro());
        payload.put("superficieM2", cotizacion.superficieM2());
        payload.put("total", cotizacion.total());
        payload.put("nombreComercial", nombre);
        GldPrincipal principal = GldPrincipal.actual();
        return crear(principal, "LICENCIA_FUNCIONAMIENTO", "EN_REVISION",
                "Licencia de funcionamiento " + nombre, payload,
                "Solicitud recibida para revisión técnica", null);
    }

    @Transactional
    public TramiteResponse licenciaConstruccion(LicenciaConstruccionRequest request) {
        if (request == null) {
            throw cuerpoVacio();
        }
        String tipoObra = exigir(request.tipoObra(), "tipoObra", "OBRA_MENOR", 80);
        if (!ClaveCatastral.valida(request.claveCatastral())) {
            throw Errores.claveCatastralInvalida();
        }
        String descripcion = exigir(request.descripcion(), "descripcion", "Ampliación de losa de azotea", 500);
        guardarAdjuntos(null, request.documentos(), true);
        pagos.exigirPredio(request.claveCatastral());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("tipoObra", tipoObra);
        payload.put("claveCatastral", request.claveCatastral());
        payload.put("descripcion", descripcion);
        GldPrincipal principal = GldPrincipal.actual();
        TramiteResponse respuesta = crear(principal, "LICENCIA_CONSTRUCCION", "RADICADO",
                "Licencia de construcción " + tipoObra, payload, "Solicitud radicada", request.documentos());
        return respuesta;
    }

    @Transactional(readOnly = true)
    public TramiteResponse porFolio(String folio) {
        return aRespuesta(buscar(folio));
    }

    @Transactional(readOnly = true)
    public List<TramiteResponse> mios() {
        List<TramiteResponse> lista = new ArrayList<>();
        for (TramiteEntity tramite : tramites.findByTenantIdAndCiudadanoIdOrderByCreadoEnDesc(
                TenantContext.exigir(), GldPrincipal.actual().id())) {
            lista.add(aRespuesta(tramite));
        }
        return lista;
    }

    @Transactional(readOnly = true)
    public List<TramiteResponse> bandeja() {
        Acceso.exigirServidor();
        List<TramiteResponse> lista = new ArrayList<>();
        for (TramiteEntity tramite : tramites.findByTenantIdAndEstadoInOrderByCreadoEnAsc(TenantContext.exigir(), BANDEJA)) {
            lista.add(aRespuesta(tramite));
        }
        return lista;
    }

    @Transactional
    public TramiteResponse dictaminar(String folio, DictamenRequest request) {
        Acceso.exigirServidor();
        if (request == null || request.decision() == null || request.decision().isBlank()) {
            throw Errores.datoInvalido("decision", "APROBAR",
                    "La decisión debe ser APROBAR u OBSERVAR. Ejemplo: APROBAR");
        }
        TramiteEntity tramite = buscar(folio);
        if (!DICTAMINABLES.contains(tramite.estado)) {
            throw Errores.estadoNoPermite();
        }
        String decision = request.decision().trim().toUpperCase(Locale.ROOT);
        GldPrincipal servidor = GldPrincipal.actual();
        if ("OBSERVAR".equals(decision)) {
            if (request.observaciones() == null || request.observaciones().isBlank()) {
                throw Errores.observacionObligatoria();
            }
            String nota = request.observaciones().trim();
            cambiar(tramite, "POR_SUBSANAR", servidor.nombre(), nota, servidor.id());
            return aRespuesta(tramite);
        }
        if (!"APROBAR".equals(decision)) {
            throw Errores.datoInvalido("decision", "APROBAR",
                    "La decisión debe ser APROBAR u OBSERVAR. Ejemplo: APROBAR");
        }
        if ("LICENCIA_FUNCIONAMIENTO".equals(tramite.tipo)) {
            JsonNode payload = leer(tramite.payload);
            BigDecimal total = payload.path("total").decimalValue();
            String nombre = payload.path("nombreComercial").asText();
            String titular = historial.findByTramiteIdOrderByCreadoEnAsc(tramite.id).stream()
                    .findFirst().map(evento -> evento.actor).orElse(nombre);
            UUID liquidacionId = pagos.liquidarLicencia(
                    tramite.tenantId, tramite.ciudadanoId, titular, tramite.folio, tramite.id, nombre, total);
            tramite.liquidacionId = liquidacionId;
            cambiar(tramite, "APROBADO_PENDIENTE_PAGO", servidor.nombre(),
                    "Aprobación técnica. Pendiente de pago.", servidor.id());
            return aRespuesta(tramite);
        }
        cambiar(tramite, "APROBADO", servidor.nombre(), "Licencia de construcción aprobada.", servidor.id());
        return aRespuesta(tramite);
    }

    @Transactional
    public TramiteResponse subsanar(String folio, SubsaniarRequest request) {
        TramiteEntity tramite = buscar(folio);
        GldPrincipal principal = GldPrincipal.actual();
        if (!tramite.ciudadanoId.equals(principal.id())) {
            throw Errores.noAutorizado();
        }
        if (!"POR_SUBSANAR".equals(tramite.estado)) {
            throw Errores.estadoNoPermite();
        }
        List<DocumentoAdjunto> documentos = request == null ? null : request.documentos();
        guardarAdjuntos(tramite.id, documentos, false);
        String estado = "LICENCIA_FUNCIONAMIENTO".equals(tramite.tipo) ? "EN_REVISION" : "RADICADO";
        cambiar(tramite, estado, principal.nombre(), "Documentos de subsanación recibidos", principal.id());
        return aRespuesta(tramite);
    }

    @Transactional
    public void marcarPagado(PagadoRequest request) {
        if (request == null || request.folio() == null || request.folio().isBlank()) {
            throw Errores.tramiteNoEncontrado();
        }
        TramiteEntity tramite = buscar(request.folio());
        if ("PAGADO".equals(tramite.estado)) {
            return;
        }
        if (!"APROBADO_PENDIENTE_PAGO".equals(tramite.estado)) {
            throw Errores.estadoNoPermite();
        }
        if (request.liquidacionId() != null) {
            tramite.liquidacionId = request.liquidacionId();
        }
        cambiar(tramite, "PAGADO", "Tesorería", "Pago acreditado", tramite.ciudadanoId);
    }

    private TramiteResponse crear(GldPrincipal principal, String tipo, String estado, String titulo,
                                  Map<String, Object> payload, String nota, List<DocumentoAdjunto> documentos) {
        Instant ahora = clock.instant();
        TramiteEntity tramite = new TramiteEntity();
        tramite.id = UUID.randomUUID();
        tramite.tenantId = TenantContext.exigir();
        tramite.folio = secuencias.folio("TRM", "tramites.folio_seq");
        tramite.tipo = tipo;
        tramite.estado = estado;
        tramite.ciudadanoId = principal.id();
        tramite.titulo = titulo.length() > 200 ? titulo.substring(0, 200) : titulo;
        tramite.payload = escribir(payload);
        tramite.creadoEn = ahora;
        tramite.actualizadoEn = ahora;
        tramites.save(tramite);
        if (documentos != null) {
            guardarAdjuntos(tramite.id, documentos, false);
        }
        anotar(tramite, estado, principal.nombre(), nota, ahora);
        bitacora.registrar(tramite.tenantId, principal.id(), "TRAMITE_CAMBIO", "TRAMITE", tramite.folio, nota);
        return aRespuesta(tramite);
    }

    private void cambiar(TramiteEntity tramite, String estado, String actor, String nota, UUID actorId) {
        Instant ahora = clock.instant();
        tramite.estado = estado;
        tramite.actualizadoEn = ahora;
        anotar(tramite, estado, actor, nota, ahora);
        bitacora.registrar(tramite.tenantId, actorId, "TRAMITE_CAMBIO", "TRAMITE", tramite.folio, nota);
    }

    private void anotar(TramiteEntity tramite, String estado, String actor, String nota, Instant ahora) {
        HistorialEntity evento = new HistorialEntity();
        evento.id = UUID.randomUUID();
        evento.tramiteId = tramite.id;
        evento.estado = estado;
        evento.actor = actor;
        evento.nota = nota.length() > 500 ? nota.substring(0, 500) : nota;
        evento.creadoEn = ahora;
        historial.save(evento);
    }

    private void guardarAdjuntos(UUID tramiteId, List<DocumentoAdjunto> documentos, boolean soloValidar) {
        if (documentos == null || documentos.isEmpty()) {
            throw Errores.adjuntoObligatorio();
        }
        List<AdjuntoEntity> listos = new ArrayList<>();
        for (DocumentoAdjunto documento : documentos) {
            if (documento == null || documento.nombre() == null || documento.nombre().isBlank()
                    || documento.contenidoBase64() == null || documento.contenidoBase64().isBlank()) {
                throw Errores.adjuntoObligatorio();
            }
            byte[] bytes;
            try {
                bytes = Base64.getDecoder().decode(documento.contenidoBase64());
            } catch (IllegalArgumentException ex) {
                throw Errores.datoInvalido("documentos", "plano.pdf",
                        "El documento no está en base64 válido. Ejemplo: plano.pdf");
            }
            if (bytes.length == 0) {
                throw Errores.adjuntoObligatorio();
            }
            if (soloValidar) {
                continue;
            }
            AdjuntoEntity adjunto = new AdjuntoEntity();
            adjunto.id = UUID.randomUUID();
            adjunto.tramiteId = tramiteId;
            String nombre = documento.nombre().trim();
            adjunto.nombre = nombre.length() > 180 ? nombre.substring(0, 180) : nombre;
            String tipo = documento.contentType() == null || documento.contentType().isBlank()
                    ? "application/octet-stream" : documento.contentType().trim();
            adjunto.contentType = tipo.length() > 80 ? tipo.substring(0, 80) : tipo;
            adjunto.contenido = bytes;
            adjunto.creadoEn = clock.instant();
            listos.add(adjunto);
        }
        if (!soloValidar) {
            adjuntos.saveAll(listos);
        }
    }

    private TramiteEntity buscar(String folio) {
        if (folio == null || folio.isBlank()) {
            throw Errores.tramiteNoEncontrado();
        }
        return tramites.findByTenantIdAndFolio(TenantContext.exigir(), folio.trim())
                .orElseThrow(Errores::tramiteNoEncontrado);
    }

    private TramiteResponse aRespuesta(TramiteEntity tramite) {
        List<EventoLinea> linea = new ArrayList<>();
        for (HistorialEntity evento : historial.findByTramiteIdOrderByCreadoEnAsc(tramite.id)) {
            linea.add(new EventoLinea(evento.estado, evento.nota, evento.actor, evento.creadoEn));
        }
        return new TramiteResponse(
                tramite.folio,
                tramite.tipo,
                tramite.estado,
                tramite.titulo,
                tramite.ciudadanoId,
                tramite.liquidacionId,
                tramite.creadoEn,
                leerMapa(tramite.payload),
                linea);
    }

    private String escribir(Map<String, Object> payload) {
        try {
            return json.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private JsonNode leer(String payload) {
        try {
            return json.readTree(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private Map<String, Object> leerMapa(String payload) {
        try {
            return json.readValue(payload, new TypeReference<>() {
            });
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String exigir(String valor, String campo, String ejemplo, int max) {
        if (valor == null || valor.isBlank()) {
            throw Errores.datoInvalido(campo, ejemplo, "El campo " + campo + " es obligatorio. Ejemplo: " + ejemplo);
        }
        String limpio = valor.trim();
        if (limpio.length() > max) {
            throw Errores.datoInvalido(campo, ejemplo,
                    "El campo " + campo + " admite hasta " + max + " caracteres. Ejemplo: " + ejemplo);
        }
        return limpio;
    }

    private static RuntimeException cuerpoVacio() {
        return Errores.datoInvalido("body", "{}", "El cuerpo de la solicitud es obligatorio. Ejemplo: {}");
    }
}
