package mx.gob.gld.documentos.domain;

import mx.gob.gld.documentos.domain.ReciboPdf;
import mx.gob.gld.documentos.domain.Sello;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelloYReciboTest {

    @Test
    void selloHmacDelFolioMontoConceptoYTitular() {
        String sello = Sello.hex(
                "PAG-2026-000001",
                new BigDecimal("3116.40"),
                "Impuesto predial 2025-6, 2026-1",
                "María López Hernández",
                "gld-sello-demo");
        assertEquals("f56a05247ca2061e1f77727ac6a10ca14a9fe9e053e939741e6d71a0a3b1b455", sello);
        assertEquals("PAG-2026-000001|3116.40|Impuesto predial 2025-6, 2026-1|María López Hernández",
                Sello.mensaje("PAG-2026-000001", new BigDecimal("3116.4"),
                        "Impuesto predial 2025-6, 2026-1", "María López Hernández"));
    }

    @Test
    void pdfContieneFolioMontoSelloYQr() throws Exception {
        String sello = Sello.hex("PAG-2026-000001", new BigDecimal("3116.40"),
                "Impuesto predial 2025-6, 2026-1", "María López Hernández", "gld-sello-demo");
        byte[] pdf = ReciboPdf.generar(
                "PAG-2026-000001",
                UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1"),
                "María López Hernández",
                "Impuesto predial 2025-6, 2026-1",
                new BigDecimal("3116.40"),
                sello,
                "http://localhost:3000/verificar/PAG-2026-000001");
        assertEquals('%', pdf[0]);
        assertEquals('P', pdf[1]);
        try (PDDocument doc = PDDocument.load(pdf)) {
            String texto = new PDFTextStripper().getText(doc);
            assertTrue(texto.contains("PAG-2026-000001"));
            assertTrue(texto.contains("3116.40"));
            assertTrue(texto.contains(sello));
            assertTrue(texto.contains("Impuesto predial 2025-6, 2026-1"));
            assertFalse(doc.getPage(0).getResources().getXObjectNames().iterator().hasNext() == false);
        }
        byte[] especial = ReciboPdf.generar("PAG-1", null, null, "linea\n\t😀", new BigDecimal("1"), "sello", "qr");
        assertEquals('%', especial[0]);
        assertEquals("", ReciboPdf.sanear(null));
        assertEquals("a b c??", ReciboPdf.sanear("a\nb\tc😀"));
    }
}
