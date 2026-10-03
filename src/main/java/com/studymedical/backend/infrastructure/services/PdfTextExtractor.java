package com.studymedical.backend.infrastructure.services;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Extrae el texto de un PDF manteniendo la página de origen.
 *
 * <p>La página se conserva para poder mostrar citas ("página N") en el frontend.</p>
 */
@Service
public class PdfTextExtractor {

    public List<PdfPage> extract(InputStream inputStream) throws IOException {
        byte[] bytes = inputStream.readAllBytes();
        try (PDDocument document = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            int total = document.getNumberOfPages();
            List<PdfPage> pages = new ArrayList<>(total);

            for (int page = 1; page <= total; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                pages.add(new PdfPage(page, stripper.getText(document)));
            }

            return pages;
        }
    }

    public record PdfPage(int pageNumber, String text) {
    }
}
