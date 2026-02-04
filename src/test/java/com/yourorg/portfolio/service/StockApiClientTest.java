package com.yourorg.portfolio.service;

import com.yourorg.portfolio.dto.StockPriceDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockApiClient Unit Tests")
class StockApiClientTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private StockApiClient stockApiClient;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.build()).thenReturn(webClient);
        ReflectionTestUtils.setField(stockApiClient, "provider", "alpha-vantage");
        ReflectionTestUtils.setField(stockApiClient, "alphaVantageApiKey", "test-key");
        ReflectionTestUtils.setField(stockApiClient, "finnhubApiKey", "test-key");
    }

    @Nested
    @DisplayName("fetchStockPrice")
    class FetchStockPriceTests {
        @Test
        void shouldReturnMockDataWhenApiFails() {
            when(webClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.error(new RuntimeException("API error")));

            StockPriceDTO result = stockApiClient.fetchStockPrice("AAPL");

            assertThat(result).isNotNull();
            assertThat(result.getSymbol()).isEqualTo("AAPL");
            assertThat(result.getCurrentPrice()).isNotNull();
            assertThat(result.getCurrentPrice()).isGreaterThan(BigDecimal.ZERO);
            assertThat(result.getPreviousClose()).isNotNull();
            assertThat(result.getChange()).isNotNull();
            assertThat(result.getTrend()).isIn("UP", "DOWN");
            assertThat(result.getCurrency()).isEqualTo("USD");
        }

        @Test
        void shouldReturnMockDataForUnknownSymbolWhenApiFails() {
            when(webClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.error(new RuntimeException("API error")));

            StockPriceDTO result = stockApiClient.fetchStockPrice("UNKNOWN123");

            assertThat(result).isNotNull();
            assertThat(result.getSymbol()).isEqualTo("UNKNOWN123");
            assertThat(result.getCurrentPrice()).isEqualByComparingTo(new BigDecimal("100.00"));
        }
    }

    @Nested
    @DisplayName("fetchTrendingStocks")
    class FetchTrendingStocksTests {
        @Test
        void shouldReturnMockTrendingStocksWhenApiFails() {
            when(webClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.error(new RuntimeException("API error")));

            List<StockPriceDTO> result = stockApiClient.fetchTrendingStocks();

            assertThat(result).isNotNull();
            assertThat(result).isNotEmpty();
            assertThat(result).hasSizeLessThanOrEqualTo(10);
            result.forEach(dto -> {
                assertThat(dto.getSymbol()).isNotNull();
                assertThat(dto.getCurrentPrice()).isNotNull();
                assertThat(dto.getCurrentPrice()).isGreaterThan(BigDecimal.ZERO);
            });
        }
    }
}
