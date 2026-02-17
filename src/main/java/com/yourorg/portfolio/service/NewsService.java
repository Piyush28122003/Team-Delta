package com.yourorg.portfolio.service;

import com.yourorg.portfolio.dto.NewsArticleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NewsService {

    private final RestTemplate restTemplate;

    @Autowired
    public NewsService() {
        this.restTemplate = new RestTemplate();
    }

    public NewsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate != null ? restTemplate : new RestTemplate();
    }

    @Value("${external.newsapi.base-url}")
    private String baseUrl;

    @Value("${external.newsapi.api-key}")
    private String apiKey;

    public List<NewsArticleDTO> getStockNews() {
        String url = baseUrl + "/everything?q=stock+market&language=en&sortBy=publishedAt&pageSize=20&apiKey=" + apiKey;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null || !"ok".equals(response.get("status"))) {
                return Collections.emptyList();
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> articles = (List<Map<String, Object>>) response.get("articles");
            if (articles == null) return Collections.emptyList();

            return articles.stream()
                    .map(this::mapToDto)
                    .filter(a -> a.getTitle() != null && !a.getTitle().equals("[Removed]"))
                    .limit(12)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private NewsArticleDTO mapToDto(Map<String, Object> article) {
        String title = (String) article.get("title");
        String description = (String) article.get("description");
        String url = (String) article.get("url");
        String urlToImage = (String) article.get("urlToImage");
        String publishedAt = (String) article.get("publishedAt");
        String sourceName = null;
        Object source = article.get("source");
        if (source instanceof Map) {
            sourceName = (String) ((Map<?, ?>) source).get("name");
        }
        if (publishedAt != null && publishedAt.length() > 10) {
            try {
                ZonedDateTime z = ZonedDateTime.parse(publishedAt);
                publishedAt = z.format(DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a"));
            } catch (Exception ignored) {}
        }
        return new NewsArticleDTO(title, description, url, urlToImage, publishedAt, sourceName);
    }
}
