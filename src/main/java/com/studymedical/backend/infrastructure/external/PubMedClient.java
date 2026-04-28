package com.studymedical.backend.infrastructure.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class PubMedClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final XmlMapper xmlMapper = new XmlMapper();

    // Paso 1: esearch (obtener IDs)
    public List<String> search(String query, int retMax, int retStart) {
        String url = String.format(
            "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=%s&retmax=%d&retstart=%d",
            query.replace(" ", "+"), retMax, retStart
        );

        try {
            String xml = restTemplate.getForObject(url, String.class);
            JsonNode root = xmlMapper.readTree(xml);
            JsonNode idList = root.path("eSearchResult").path("IdList");
            
            List<String> ids = new ArrayList<>();
            if (idList.isArray()) {
                idList.forEach(node -> ids.add(node.asText()));
            }
            return ids;
        } catch (Exception e) {
            log.warn("PubMed search failed for query: {}", query, e);
            return List.of();
        }
    }

    // Paso 2: efetch (abstract COMPLETO - crítico!)
    public List<PubMedArticle> fetchDetails(List<String> ids) {
        if (ids == null || ids.isEmpty()) return List.of();

        String url = String.format(
            "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=%s&retmode=xml",
            String.join(",", ids)
        );

        try {
            String xml = restTemplate.getForObject(url, String.class);
            JsonNode root = xmlMapper.readTree(xml);
            JsonNode articles = root.path("PubmedArticleSet").path("PubmedArticle");

            List<PubMedArticle> results = new ArrayList<>();
            if (articles.isArray()) {
                articles.forEach(article -> results.add(parseArticle(article)));
            } else if (!articles.isMissingNode()) {
                results.add(parseArticle(articles));
            }
            return results;
        } catch (Exception e) {
            log.warn("PubMed efetch failed for ids: {}", ids, e);
            return List.of();
        }
    }

    private PubMedArticle parseArticle(JsonNode node) {
        PubMedArticle article = new PubMedArticle();
        
        JsonNode medline = node.path("MedlineCitation");
        article.setId(medline.path("PMID").path("#text").asText(""));
        
        JsonNode articleNode = medline.path("Article");
        article.setTitle(articleNode.path("ArticleTitle").asText(""));
        
        // Abstract completo
        JsonNode abstractNode = articleNode.path("Abstract").path("AbstractText");
        if (abstractNode.isArray()) {
            StringBuilder sb = new StringBuilder();
            abstractNode.forEach(n -> sb.append(n.asText()).append(" "));
            article.setAbstractText(sb.toString().trim());
        } else {
            article.setAbstractText(abstractNode.asText(""));
        }
        
        // Authors
        List<String> authors = new ArrayList<>();
        JsonNode authorList = articleNode.path("AuthorList").path("Author");
        if (authorList.isArray()) {
            authorList.forEach(a -> authors.add(a.path("LastName").asText("") + " " + a.path("Initials").asText("")).trim()));
        }
        article.setAuthors(authors);
        
        // Publication date
        JsonNode dateNode = medline.path("DateCompleted");
        if (!dateNode.isMissingNode()) {
            String year = dateNode.path("Year").asText("");
            String month = dateNode.path("Month").asText("");
            article.setPubDate(year + (month.isEmpty() ? "" : "-" + month));
        }
        
        article.setUrl("https://pubmed.ncbi.nlm.nih.gov/" + article.getId());
        return article;
    }

    // DTO interno
    public static class PubMedArticle {
        private String id;
        private String title;
        private String abstractText;
        private List<String> authors;
        private String pubDate;
        private String url;

        // Getters y setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getAbstractText() { return abstractText; }
        public void setAbstractText(String abstractText) { this.abstractText = abstractText; }
        public List<String> getAuthors() { return authors; }
        public void setAuthors(List<String> authors) { this.authors = authors; }
        public String getPubDate() { return pubDate; }
        public void setPubDate(String pubDate) { this.pubDate = pubDate; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
}
