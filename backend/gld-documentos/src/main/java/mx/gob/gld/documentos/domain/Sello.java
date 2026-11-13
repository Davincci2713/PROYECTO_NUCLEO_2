package mx.gob.gld.documentos.domain;

import mx.gob.gld.common.money.Dinero;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;

public final class Sello {

    private Sello() {
    }

    public static String mensaje(String folio, BigDecimal monto, String concepto, String titular) {
        return folio + "|" + Dinero.dos(monto).toPlainString() + "|" + concepto + "|" + titular;
    }

    public static String hex(String folio, BigDecimal monto, String concepto, String titular, String secreto) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] firma = mac.doFinal(mensaje(folio, monto, concepto, titular).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(firma);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
