package mx.gob.gld.documentos.domain;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import mx.gob.gld.common.money.Dinero;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.UUID;

public final class ReciboPdf {

    private ReciboPdf() {
    }

    public static byte[] generar(String folio, UUID transaccionId, String titular, String concepto,
                                 BigDecimal monto, String sello, String qrPayload) {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);
            PDImageXObject qr = PDImageXObject.createFromByteArray(doc, qr(qrPayload), "qr");
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
                cs.newLineAtOffset(50, 740);
                linea(cs, "Recibo oficial GLD");
                cs.setFont(PDType1Font.HELVETICA, 12);
                cs.newLineAtOffset(0, -28);
                linea(cs, "Folio: " + folio);
                linea(cs, "Transacción: " + transaccionId);
                linea(cs, "Titular: " + titular);
                linea(cs, "Concepto: " + concepto);
                linea(cs, "Monto: " + Dinero.dos(monto).toPlainString());
                linea(cs, "Sello: " + sello);
                cs.endText();
                cs.drawImage(qr, 50, 460, 140, 140);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo generar el PDF", ex);
        }
    }

    private static void linea(PDPageContentStream cs, String texto) throws Exception {
        cs.showText(sanear(texto));
        cs.newLineAtOffset(0, -18);
    }

    static String sanear(String valor) {
        if (valor == null) {
            return "";
        }
        StringBuilder salida = new StringBuilder();
        for (int i = 0; i < valor.length(); i++) {
            char c = valor.charAt(i);
            if (c == '\n' || c == '\r' || c == '\t') {
                salida.append(' ');
            } else if (c >= 32 && c <= 255) {
                salida.append(c);
            } else {
                salida.append('?');
            }
        }
        return salida.toString();
    }

    private static byte[] qr(String payload) throws Exception {
        BitMatrix matrix = new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 200, 200);
        BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", out);
        return out.toByteArray();
    }
}
