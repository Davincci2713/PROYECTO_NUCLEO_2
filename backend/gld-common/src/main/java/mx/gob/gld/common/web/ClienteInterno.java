package mx.gob.gld.common.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import mx.gob.gld.common.error.Errores;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

public final class ClienteInterno {

    private ClienteInterno() {
    }

    public static <T> T get(RestClient http, ObjectMapper json, String url, Class<T> tipo, String autorizacion, String tenant) {
        try {
            return http.get()
                    .uri(url)
                    .header("Authorization", autorizacion)
                    .header("X-Tenant-ID", tenant)
                    .retrieve()
                    .body(tipo);
        } catch (RestClientResponseException ex) {
            throw Errores.traducir(ex, json);
        } catch (RestClientException ex) {
            throw Errores.servicioNoDisponible();
        }
    }

    public static <T> T getInterno(RestClient http, ObjectMapper json, String url, Class<T> tipo, String token, String tenant) {
        try {
            return http.get()
                    .uri(url)
                    .header("X-Internal-Token", token)
                    .header("X-Tenant-ID", tenant)
                    .retrieve()
                    .body(tipo);
        } catch (RestClientResponseException ex) {
            throw Errores.traducir(ex, json);
        } catch (RestClientException ex) {
            throw Errores.servicioNoDisponible();
        }
    }

    public static <T> T post(RestClient http, ObjectMapper json, String url, Object cuerpo, Class<T> tipo, String autorizacion, String tenant) {
        try {
            return http.post()
                    .uri(url)
                    .header("Authorization", autorizacion)
                    .header("X-Tenant-ID", tenant)
                    .body(cuerpo)
                    .retrieve()
                    .body(tipo);
        } catch (RestClientResponseException ex) {
            throw Errores.traducir(ex, json);
        } catch (RestClientException ex) {
            throw Errores.servicioNoDisponible();
        }
    }

    public static <T> T postInterno(RestClient http, ObjectMapper json, String url, Object cuerpo, Class<T> tipo, String token, String tenant) {
        try {
            return http.post()
                    .uri(url)
                    .header("X-Internal-Token", token)
                    .header("X-Tenant-ID", tenant)
                    .body(cuerpo)
                    .retrieve()
                    .body(tipo);
        } catch (RestClientResponseException ex) {
            throw Errores.traducir(ex, json);
        } catch (RestClientException ex) {
            throw Errores.servicioNoDisponible();
        }
    }

    public static void postInternoVacio(RestClient http, ObjectMapper json, String url, Object cuerpo, String token, String tenant) {
        try {
            http.post()
                    .uri(url)
                    .header("X-Internal-Token", token)
                    .header("X-Tenant-ID", tenant)
                    .body(cuerpo)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw Errores.traducir(ex, json);
        } catch (RestClientException ex) {
            throw Errores.servicioNoDisponible();
        }
    }
}
