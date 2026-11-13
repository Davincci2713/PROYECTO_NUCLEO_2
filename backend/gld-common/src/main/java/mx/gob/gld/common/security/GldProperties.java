package mx.gob.gld.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gld")
public class GldProperties {

    private final Jwt jwt = new Jwt();
    private String internalToken;
    private String selloSecret = "gld-sello-demo";
    private String verificacionBase = "http://localhost:3000/verificar";
    private final Services services = new Services();

    public Jwt getJwt() {
        return jwt;
    }

    public String getInternalToken() {
        return internalToken;
    }

    public void setInternalToken(String internalToken) {
        this.internalToken = internalToken;
    }

    public String getSelloSecret() {
        return selloSecret;
    }

    public void setSelloSecret(String selloSecret) {
        this.selloSecret = selloSecret;
    }

    public String getVerificacionBase() {
        return verificacionBase;
    }

    public void setVerificacionBase(String verificacionBase) {
        this.verificacionBase = verificacionBase;
    }

    public Services getServices() {
        return services;
    }

    public static class Jwt {
        private String secret;
        private long sessionSeconds = 3600;
        private long preTokenSeconds = 300;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getSessionSeconds() {
            return sessionSeconds;
        }

        public void setSessionSeconds(long sessionSeconds) {
            this.sessionSeconds = sessionSeconds;
        }

        public long getPreTokenSeconds() {
            return preTokenSeconds;
        }

        public void setPreTokenSeconds(long preTokenSeconds) {
            this.preTokenSeconds = preTokenSeconds;
        }
    }

    public static class Services {
        private String identity = "http://localhost:8081";
        private String catastro = "http://localhost:8082";
        private String pagos = "http://localhost:8083";
        private String tramites = "http://localhost:8084";
        private String documentos = "http://localhost:8085";
        private String ciudad = "http://localhost:8086";
        private String incidencias = "http://localhost:8087";

        public String getIdentity() {
            return identity;
        }

        public void setIdentity(String identity) {
            this.identity = identity;
        }

        public String getCatastro() {
            return catastro;
        }

        public void setCatastro(String catastro) {
            this.catastro = catastro;
        }

        public String getPagos() {
            return pagos;
        }

        public void setPagos(String pagos) {
            this.pagos = pagos;
        }

        public String getTramites() {
            return tramites;
        }

        public void setTramites(String tramites) {
            this.tramites = tramites;
        }

        public String getDocumentos() {
            return documentos;
        }

        public void setDocumentos(String documentos) {
            this.documentos = documentos;
        }

        public String getCiudad() {
            return ciudad;
        }

        public void setCiudad(String ciudad) {
            this.ciudad = ciudad;
        }

        public String getIncidencias() {
            return incidencias;
        }

        public void setIncidencias(String incidencias) {
            this.incidencias = incidencias;
        }
    }
}
