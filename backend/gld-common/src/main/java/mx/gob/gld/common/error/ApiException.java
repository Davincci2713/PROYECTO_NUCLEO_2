package mx.gob.gld.common.error;

public class ApiException extends RuntimeException {

    private final int status;
    private final String titulo;
    private final String detalle;
    private final String campo;
    private final String ejemplo;
    private final String codigo;

    public ApiException(int status, String titulo, String detalle, String campo, String ejemplo, String codigo) {
        super(detalle);
        this.status = status;
        this.titulo = titulo;
        this.detalle = detalle;
        this.campo = campo;
        this.ejemplo = ejemplo;
        this.codigo = codigo;
    }

    public static ApiException of(int status, String titulo, String detalle, String campo, String ejemplo, String codigo) {
        return new ApiException(status, titulo, detalle, campo, ejemplo, codigo);
    }

    public static ApiException de(ApiError error) {
        return new ApiException(error.status(), error.titulo(), error.detalle(), error.campo(), error.ejemplo(), error.codigo());
    }

    public ApiError cuerpo() {
        return new ApiError(titulo, status, detalle, campo, ejemplo, codigo);
    }

    public int getStatus() {
        return status;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDetalle() {
        return detalle;
    }

    public String getCampo() {
        return campo;
    }

    public String getEjemplo() {
        return ejemplo;
    }

    public String getCodigo() {
        return codigo;
    }
}
