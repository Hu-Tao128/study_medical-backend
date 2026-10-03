package com.studymedical.backend.infrastructure.services;

import com.studymedical.backend.infrastructure.services.PdfTextExtractor.PdfPage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfTextExtractorTest {

    private final PdfTextExtractor extractor = new PdfTextExtractor();

    @Test
    void shouldExtractTextAndPageNumberFromSinglePagePdf() throws IOException {
        byte[] pdf = createPdf(List.of("Hola mundo desde el PDF"));

        List<PdfPage> pages = extractor.extract(new ByteArrayInputStream(pdf));

        assertEquals(1, pages.size());
        assertEquals(1, pages.get(0).pageNumber());
        assertTrue(pages.get(0).text().contains("Hola mundo desde el PDF"));
    }

    @Test
    void shouldExtractMultiplePagesInOrder() throws IOException {
        byte[] pdf = createPdf(List.of("Primera pagina", "Segunda pagina"));

        List<PdfPage> pages = extractor.extract(new ByteArrayInputStream(pdf));

        assertEquals(2, pages.size());
        assertEquals(1, pages.get(0).pageNumber());
        assertEquals(2, pages.get(1).pageNumber());
        assertTrue(pages.get(0).text().contains("Primera pagina"));
        assertTrue(pages.get(1).text().contains("Segunda pagina"));
    }

    private byte[] createPdf(List<String> pageTexts) throws IOException {
        try (PDDocument document = new PDDocument()) {
            for (String text : pageTexts) {
                PDPage page = new PDPage();
                document.addPage(page);
                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    content.beginText();
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    content.newLineAtOffset(50, 700);
                    content.showText(text);
                    content.endText();
                }
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            document.save(output);
            return output.toByteArray();
        }
    }
}
