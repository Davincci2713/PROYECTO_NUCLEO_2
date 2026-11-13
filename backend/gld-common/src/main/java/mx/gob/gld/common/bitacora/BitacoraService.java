package mx.gob.gld.common.bitacora;

import java.time.Instant;
import java.util.UUID;

public class BitacoraService {

    private final BitacoraRepository repository;

    public BitacoraService(BitacoraRepository repository) {
        this.repository = repository;
    }

    public void registrar(String tenantId, UUID actorId, String accion, String recurso, String referencia, String detalle) {
        BitacoraEntity fila = new BitacoraEntity();
        fila.tenantId = tenantId;
        fila.ocurrioEn = Instant.now();
        fila.actorId = actorId;
        fila.accion = accion;
        fila.recurso = recurso;
        fila.referencia = recortar(referencia, 80);
        fila.detalle = recortar(detalle, 500);
        repository.save(fila);
    }

    private static String recortar(String valor, int max) {
        if (valor == null) {
            return null;
        }
        return valor.length() <= max ? valor : valor.substring(0, max);
    }
}
