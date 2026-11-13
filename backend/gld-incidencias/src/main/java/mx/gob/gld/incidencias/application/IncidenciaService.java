package mx.gob.gld.incidencias.application;

import mx.gob.gld.common.bitacora.BitacoraService;
import mx.gob.gld.common.error.Errores;
import mx.gob.gld.common.money.Secuencias;
import mx.gob.gld.common.security.GldPrincipal;
import mx.gob.gld.common.security.TenantContext;
import mx.gob.gld.incidencias.domain.Coordenadas;
import mx.gob.gld.incidencias.infrastructure.BrigadaEntity;
import mx.gob.gld.incidencias.infrastructure.BrigadaRepository;
import mx.gob.gld.incidencias.infrastructure.IncidenciaEntity;
import mx.gob.gld.incidencias.infrastructure.IncidenciaRepository;
import mx.gob.gld.incidencias.web.IncidenciaDtos.AltaRequest;
import mx.gob.gld.incidencias.web.IncidenciaDtos.BrigadaResponse;
import mx.gob.gld.incidencias.web.IncidenciaDtos.IncidenciaResponse;
import mx.gob.gld.incidencias.web.IncidenciaDtos.VistaPreviaResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class IncidenciaService {

    private static final Set<String> TIPOS = Set.of("BACHE", "ALUMBRADO", "AGUA", "BASURA");

    private final IncidenciaRepository incidencias;
    private final BrigadaRepository brigadas;
    private final Secuencias secuencias;
    private final BitacoraService bitacora;
    private final Clock clock;

    public IncidenciaService(IncidenciaRepository incidencias, BrigadaRepository brigadas, Secuencias secuencias,
                             BitacoraService bitacora, Clock clock) {
        this.incidencias = incidencias;
        this.brigadas = brigadas;
        this.secuencias = secuencias;
        this.bitacora = bitacora;
        this.clock = clock;
    }

    public VistaPreviaResponse vistaPrevia(BigDecimal lat, BigDecimal lng) {
        BigDecimal latitud = Coordenadas.latitud(lat);
        BigDecimal longitud = Coordenadas.longitud(lng);
        return new VistaPreviaResponse(Coordenadas.referencia(latitud, longitud));
    }

    @Transactional
    public IncidenciaResponse reportar(AltaRequest request) {
        if (request == null) {
            throw Errores.datoInvalido("body", "{}", "El cuerpo de la solicitud es obligatorio. Ejemplo: {}");
        }
        String tipo = tipo(request.tipo());
        BigDecimal lat = Coordenadas.latitud(request.lat());
        BigDecimal lng = Coordenadas.longitud(request.lng());
        String direccion = exigir(request.direccionReferencia(), "direccionReferencia",
                "Av. Reforma 120, frente al número 118", 240);
        String descripcion = exigir(request.descripcion(), "descripcion", "Bache profundo en el carril derecho", 800);
        String tenant = TenantContext.exigir();
        BrigadaEntity brigada = brigadas.findByTenantIdAndTipo(tenant, tipo)
                .orElseThrow(() -> Errores.noEncontrado("No hay una brigada para el tipo " + tipo + "."));
        GldPrincipal principal = GldPrincipal.actual();
        IncidenciaEntity incidencia = new IncidenciaEntity();
        incidencia.id = UUID.randomUUID();
        incidencia.tenantId = tenant;
        incidencia.folio = secuencias.folio("INC", "incidencias.folio_seq");
        incidencia.tipo = tipo;
        incidencia.estado = "ASIGNADA";
        incidencia.ciudadanoId = principal.id();
        incidencia.lat = lat;
        incidencia.lng = lng;
        incidencia.direccionReferencia = direccion;
        incidencia.descripcion = descripcion;
        aplicarFoto(incidencia, request.fotoBase64(), request.fotoContentType());
        incidencia.brigadaId = brigada.id;
        incidencia.creadaEn = clock.instant();
        incidencias.save(incidencia);
        bitacora.registrar(tenant, principal.id(), "INCIDENCIA_REPORTADA", "INCIDENCIA", incidencia.folio,
                tipo + " en " + direccion);
        return aRespuesta(incidencia, brigada, null);
    }

    @Transactional(readOnly = true)
    public IncidenciaResponse porFolio(String folio) {
        IncidenciaEntity incidencia = incidencias.findByTenantIdAndFolio(TenantContext.exigir(), folio)
                .orElseThrow(() -> Errores.noEncontrado("No hay una incidencia con ese folio. Ejemplo: INC-2026-000001"));
        return aRespuesta(incidencia, brigadaDe(incidencia), tieneFoto(incidencia));
    }

    @Transactional(readOnly = true)
    public List<IncidenciaResponse> mias() {
        List<IncidenciaResponse> lista = new ArrayList<>();
        for (IncidenciaEntity incidencia : incidencias.findByTenantIdAndCiudadanoIdOrderByCreadaEnDesc(
                TenantContext.exigir(), GldPrincipal.actual().id())) {
            lista.add(aRespuesta(incidencia, brigadaDe(incidencia), tieneFoto(incidencia)));
        }
        return lista;
    }

    @Transactional(readOnly = true)
    public IncidenciaEntity foto(UUID id) {
        IncidenciaEntity incidencia = incidencias.findByTenantIdAndId(TenantContext.exigir(), id)
                .orElseThrow(() -> Errores.noEncontrado("La incidencia no tiene foto."));
        if (!tieneFoto(incidencia)) {
            throw Errores.noEncontrado("La incidencia no tiene foto.");
        }
        return incidencia;
    }

    private BrigadaEntity brigadaDe(IncidenciaEntity incidencia) {
        return brigadas.findById(incidencia.brigadaId)
                .orElseThrow(() -> Errores.noEncontrado("No hay una brigada asignada."));
    }

    private static void aplicarFoto(IncidenciaEntity incidencia, String base64, String contentType) {
        if (base64 == null || base64.isBlank()) {
            return;
        }
        try {
            incidencia.foto = Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException ex) {
            throw Errores.datoInvalido("fotoBase64", "JVBERg==",
                    "La foto no está en base64 válido. Ejemplo: JVBERg==");
        }
        String tipo = contentType == null || contentType.isBlank() ? "image/jpeg" : contentType.trim();
        incidencia.fotoContentType = tipo.length() > 80 ? tipo.substring(0, 80) : tipo;
    }

    private static IncidenciaResponse aRespuesta(IncidenciaEntity incidencia, BrigadaEntity brigada, Boolean tieneFoto) {
        return new IncidenciaResponse(
                incidencia.folio,
                incidencia.tipo,
                incidencia.estado,
                incidencia.direccionReferencia,
                incidencia.lat,
                incidencia.lng,
                incidencia.descripcion,
                new BrigadaResponse(brigada.nombre, brigada.zona),
                incidencia.creadaEn,
                tieneFoto);
    }

    private static boolean tieneFoto(IncidenciaEntity incidencia) {
        return incidencia.foto != null && incidencia.foto.length > 0;
    }

    private static String tipo(String tipo) {
        if (tipo == null || !TIPOS.contains(tipo.trim().toUpperCase(Locale.ROOT))) {
            throw Errores.datoInvalido("tipo", "BACHE",
                    "El tipo de incidencia debe ser BACHE, ALUMBRADO, AGUA o BASURA. Ejemplo: BACHE");
        }
        return tipo.trim().toUpperCase(Locale.ROOT);
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
}
