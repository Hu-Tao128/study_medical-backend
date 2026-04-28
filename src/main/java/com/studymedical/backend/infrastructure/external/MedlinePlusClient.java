package com.studymedical.backend.infrastructure.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedlinePlusClient {

    private final RestTemplate restTemplate;
    private final XmlMapper xmlMapper = new XmlMapper();

    public List<MedlineDocument> searchHealthTopics(String term) {
        String url = String.format(
            "https://wsearch.nlm.nih.gov/ws/query?db=healthTopics&term=%s",
            term.replace(" ", "+")
        );

        try {
            String xml = restTemplate.getForObject(url, String.class);
            JsonNode root = xmlMapper.readTree(xml);
            JsonNode documents = root.path("nlmSearchResult").path("list").path("document");

            List<MedlineDocument> results = new ArrayList<>();
            if (documents.isArray()) {
                documents.forEach(doc -> results.add(parseDocument(doc)));
            }
            return results;
        } catch (Exception e) {
            log.warn("MedlinePlus search failed for term: {}", term, e);
            return List.of();
        }
    }

    private MedlineDocument parseDocument(JsonNode node) {
        MedlineDocument doc = new MedlineDocument();
        doc.setUrl(node.path("@url").asText(""));
        doc.setRank(node.path("@rank").asInt(0));

        node.path("content").forEach(content -> {
            String name = content.path("@name").asText("");
            String value = content.asText("");
            switch (name) {
                case "title" -> doc.setTitle(value);
                case "FullSummary" -> doc.setFullSummary(value);
                case "snippet" -> doc.setSnippet(value);
            }
        });

        return doc;
    }

    // DTO interno
    public static class MedlineDocument {
        private String url;
        private int rank;
        private String title;
        private String fullSummary;
        private String snippet;

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public int getRank() { return rank; }
        public void setRank(int rank) { this.rank = rank; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getFullSummary() { return fullSummary; }
        public void setFullSummary(String fullSummary) { this.fullSummary = fullSummary; }
        public String getSnippet() { return snippet; }
        public void setSnippet(String snippet) { this.snippet = snippet; }
    }
}
