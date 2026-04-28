package com.studymedical.backend.domain.services;

import com.studymedical.backend.domain.entities.*;
import org.jsoup.Jsoup;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SearchResultNormalizer {

    // PubMed → MedicalSearchResult (con abstract completo)
    public static MedicalSearchResult fromPubMed(String id, String title, String abstractText,
                                                  String pubDate, String url,
                                                  List<String> authors) {
        return MedicalSearchResult.builder()
                .id(id)
                .title(cleanHtml(title))
                .description(cleanHtml(abstractText))
                .source(SearchSource.NIH_PUBMED)
                .url(url != null ? url : "https://pubmed.ncbi.nlm.nih.gov/" + id)
                .authors(authors != null ? authors : List.of())
                .publicationDate(pubDate)
                .contentType(ContentType.ARTICLE)
                .build();
    }

    // MedlinePlus → MedicalSearchResult (definiciones)
    public static MedicalSearchResult fromMedlinePlus(String url, String title,
                                                     String summary, String contentType) {
        return MedicalSearchResult.builder()
                .id(extractIdFromUrl(url))
                .title(cleanHtml(title))
                .description(cleanHtml(summary))
                .source(SearchSource.NIH_MEDLINE)
                .url(url)
                .contentType(ContentType.DEFINITION)
                .build();
    }

    // Local (Supabase) → MedicalSearchResult
    public static MedicalSearchResult fromLocal(com.studymedical.backend.domain.entities.MedicalChunk chunk) {
        return MedicalSearchResult.builder()
                .id(chunk.getId().toString())
                .title(cleanHtml(chunk.getChunkTitle()))
                .description(cleanHtml(chunk.getChunkText()))
                .source(SearchSource.LOCAL)
                .authors(chunk.getAuthor() != null ? List.of(chunk.getAuthor()) : List.of())
                .bookTitle(chunk.getBook())
                .edition(chunk.getEdition())
                .contentType(ContentType.BOOK)
                .build();
    }

    // 🆕 Limpieza HTML brutal
    private static String cleanHtml(String html) {
        if (html == null || html.isEmpty()) return "";
        return Jsoup.parse(html).text().trim();
    }

    private static String extractIdFromUrl(String url) {
        if (url == null) return java.util.UUID.randomUUID().toString();
        String[] parts = url.split("/");
        return parts[parts.length - 1].replace(".html", "");
    }
}
