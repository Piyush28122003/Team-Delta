package com.yourorg.portfolio.service;

import com.yourorg.portfolio.dto.NewsArticleDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NewsService Unit Tests")
class NewsServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private NewsService newsService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(newsService, "baseUrl", "https://newsapi.org/v2");
        ReflectionTestUtils.setField(newsService, "apiKey", "test-api-key");
    }

    @Nested
    @DisplayName("getStockNews")
    class GetStockNewsTests {
        @Test
        void shouldReturnEmptyListWhenResponseIsNull() {
            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(null);

            List<NewsArticleDTO> result = newsService.getStockNews();

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmptyListWhenStatusIsNotOk() {
            Map<String, Object> response = Map.of("status", "error");
            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

            List<NewsArticleDTO> result = newsService.getStockNews();

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmptyListWhenArticlesIsNull() {
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("status", "ok");
            response.put("articles", null);
            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

            List<NewsArticleDTO> result = newsService.getStockNews();

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnArticlesWhenResponseIsValid() {
            Map<String, Object> source = Map.of("name", "Reuters");
            Map<String, Object> article = Map.of(
                    "title", "Stock Market News",
                    "description", "Market update",
                    "url", "https://example.com/article",
                    "urlToImage", "https://example.com/image.jpg",
                    "publishedAt", "2024-01-15T10:00:00Z",
                    "source", source
            );
            Map<String, Object> response = Map.of(
                    "status", "ok",
                    "articles", List.of(article)
            );
            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

            List<NewsArticleDTO> result = newsService.getStockNews();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Stock Market News");
            assertThat(result.get(0).getDescription()).isEqualTo("Market update");
            assertThat(result.get(0).getSourceName()).isEqualTo("Reuters");
        }

        @Test
        void shouldFilterOutRemovedArticles() {
            Map<String, Object> source = Map.of("name", "Reuters");
            Map<String, Object> removedArticle = Map.of(
                    "title", "[Removed]",
                    "description", "",
                    "url", "",
                    "urlToImage", "",
                    "publishedAt", "",
                    "source", source
            );
            Map<String, Object> response = Map.of(
                    "status", "ok",
                    "articles", List.of(removedArticle)
            );
            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

            List<NewsArticleDTO> result = newsService.getStockNews();

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmptyListWhenApiThrowsException() {
            when(restTemplate.getForObject(anyString(), eq(Map.class)))
                    .thenThrow(new RuntimeException("Network error"));

            List<NewsArticleDTO> result = newsService.getStockNews();

            assertThat(result).isEmpty();
        }

        @Test
        void shouldLimitResultsToTwelve() {
            Map<String, Object> source = Map.of("name", "Reuters");
            Map<String, Object> article = Map.of(
                    "title", "Article",
                    "description", "Desc",
                    "url", "https://example.com",
                    "urlToImage", "",
                    "publishedAt", "2024-01-15T10:00:00Z",
                    "source", source
            );
            List<Map<String, Object>> articles = Collections.nCopies(20, article);
            Map<String, Object> response = Map.of("status", "ok", "articles", (Object) articles);
            when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(response);

            List<NewsArticleDTO> result = newsService.getStockNews();

            assertThat(result).hasSize(12);
        }
    }
}
